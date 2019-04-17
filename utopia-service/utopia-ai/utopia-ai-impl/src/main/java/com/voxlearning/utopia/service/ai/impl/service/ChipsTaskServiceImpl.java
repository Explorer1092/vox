package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsTaskService;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.constant.ChipsUserDrawingTaskStatus;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.StoneQuestionData;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTaskJoin;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.entity.reddot.ChipsRedDotPage;
import com.voxlearning.utopia.service.ai.entity.reddot.ChipsUserRedDotPageRecord;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.reddot.ChipsRedDotPagePersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.reddot.ChipsUserRedDotRecordPagePersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskJoinPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskPersistence;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Named
@ExposeService(interfaceClass = ChipsTaskService.class)
public class ChipsTaskServiceImpl implements ChipsTaskService {
    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsUserDrawingTaskPersistence chipsUserDrawingTaskPersistence;

    @Inject
    private ChipsUserDrawingTaskJoinPersistence chipsUserDrawingTaskJoinPersistence;

    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    @Inject
    private ChipsRedDotPagePersistence chipsRedDotPagePersistence;

    @Inject
    private ChipsUserRedDotRecordPagePersistence chipsUserRedDotRecordPagePersistence;

    @Override
    public MapMessage processDrawingTaskJoin(Long drawingTaskId, String openId, String userAnswer) {
        if (drawingTaskId == null || StringUtils.isAnyBlank(openId, userAnswer)) {
            return MapMessage.errorMessage("参数为空");
        }

        ChipsUserDrawingTask task = chipsUserDrawingTaskPersistence.load(drawingTaskId);
        if (task == null) {
            return MapMessage.errorMessage("未找到图鉴任务");
        }

        StoneQuestionData taskData = Optional.ofNullable(task.getDrawingId())
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(e)))
                .map(map -> map.get(task.getDrawingId()))
                .map(StoneQuestionData::newInstance)
                .filter(data -> MapUtils.isNotEmpty(data.getJsonData()))
                .orElse(null);

        if (task == null) {
            return MapMessage.errorMessage("未找到图鉴");
        }

        boolean master = userAnswer.equals(SafeConverter.toString(taskData.getJsonData().get("right_answer")));
        try {
            ChipsWechatUserEntity wechatUserEntity = wechatUserPersistence.loadByOpenIdAndType(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.getCode());
            if (wechatUserEntity != null && !task.getUserId().equals(wechatUserEntity.getUserId())) {
                chipsUserDrawingTaskJoinPersistence.insertOrUpdate(wechatUserEntity.getId(), task.getId(), userAnswer, master);
                if (!ChipsUserDrawingTaskStatus.finished.name().equals(task.getStatus())) {
                    List<ChipsUserDrawingTaskJoin> taskJoinList = chipsUserDrawingTaskJoinPersistence.loadByTask(task.getId());
                    int total = taskJoinList.stream().mapToInt(ChipsUserDrawingTaskJoin::getEnergy).sum();
                    if (total >= task.getTotalEnergy()) {
                        chipsUserDrawingTaskPersistence.updateStatus(task, ChipsUserDrawingTaskStatus.finished);
                        userPageVisitCacheManager.addRecord(task.getId(), ConstantSupport.DRAWING_TASK_FINISH_CACHE_KEY + task.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("processDrawingTaskJoin error, drawingTaskId:{}, openId:{}, userAnswer:{}", drawingTaskId, openId, userAnswer, e);
        }

        return MapMessage.successMessage().set("master", master);
    }

    @Override
    public MapMessage processPageRedDotRead(String pageCode, Long userId) {
        ChipsRedDotPage chipsRedDotPage = chipsRedDotPagePersistence.loadByCode(pageCode);

        if (chipsRedDotPage == null) {
            return MapMessage.errorMessage("页面不存在或者删除");
        }

        ChipsUserRedDotPageRecord record = chipsUserRedDotRecordPagePersistence.loadByUser(userId).stream().filter(e -> e.getPage().equals(chipsRedDotPage.getId())).findFirst().orElse(null);

        if (record == null) {
            chipsUserRedDotRecordPagePersistence.insertOrUpdate(userId, chipsRedDotPage.getId(), true);
        }

        return MapMessage.successMessage();
    }
}
