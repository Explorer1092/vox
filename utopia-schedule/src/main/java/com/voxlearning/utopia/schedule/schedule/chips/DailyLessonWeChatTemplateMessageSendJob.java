package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.AiOrderProductService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.wechat.api.constants.BooKConst;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author guangqing
 * @since 2018/8/3
 * 该功能的设计是基于用户购买的产品只有一个短期课，售卖正式课之后该逻辑走不通了，故暂停使用，
 * 如需使用需要重新设计逻辑 重新开放
 */
@Named
@ScheduledJobDefinition(
        jobName = "今日学习公众号推送",
        jobDescription = "每天20点执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 20 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
@Deprecated
public class DailyLessonWeChatTemplateMessageSendJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AiLoaderClient aiLoaderClient;
    @ImportService(interfaceClass = AiOrderProductService.class)
    private AiOrderProductService aiOrderProductService;
    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;
    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    /**
     * 配置格式样例：{"userIds":"262316,262870"}
     *
     * @param parameters
     * @return
     */
    private List<Long> loadUserIdFromParameters(Map<String, Object> parameters) {
        if (MapUtils.isEmpty(parameters)) {
            return null;
        }
        Object userIdsObj = parameters.get("userIds");
        if (userIdsObj == null) {
            return null;
        }
        String userIdStr = userIdsObj.toString();
        if (StringUtils.isBlank(userIdStr)) {
            return null;
        }
        String[] split = userIdStr.split(",");
        List<Long> list = new ArrayList<>();
        for (String idStr : split) {
            long userId = SafeConverter.toLong(idStr);
            if (0l == userId) {
                continue;
            }
            list.add(userId);
        }
        return list;
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        OrderProduct product = aiOrderProductService.loadCurrentValidProduct();
        if (product == null || StringUtils.isBlank(product.getAttributes())) {
            return;
        }
        List<ChipsEnglishClass> clazzList = loadClazzByOrderProduct(product);
        if (CollectionUtils.isEmpty(clazzList)) {
            return;
        }
        List<Long> userIdList = loadUserIdFromParameters(parameters);
        if (CollectionUtils.isEmpty(userIdList)) {//没有通过参数传的时候走正常的发送流程
            userIdList = loadAllUserId(clazzList);
        }
        if (CollectionUtils.isEmpty(userIdList)) {
            return;
        }
        StoneUnitData currentUnit = chipsEnglishContentLoader.loadTodayStudyUnit(product.getId());
        if (currentUnit == null || StringUtils.isBlank(currentUnit.getId())) {
            return;
        }
        userIdList.forEach(u -> aiLoaderClient.getRemoteReference().sendDailyLessonTemplateMessage(u, currentUnit, product, BooKConst.CHIPS_ENGLISH_BOOK_ID));
    }

    private List<Long> loadAllUserId(List<ChipsEnglishClass> clazzList) {
        if (CollectionUtils.isEmpty(clazzList)) {
            return null;
        }
        List<Long> userIdList = new ArrayList<>();
        for (ChipsEnglishClass clazz : clazzList) {
            List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazz.getId());
            if (CollectionUtils.isEmpty(userRefList)) {
                continue;
            }
            userRefList.forEach(e -> userIdList.add(e.getUserId()));
        }
        return userIdList;
    }

    private List<ChipsEnglishClass> loadClazzByOrderProduct(OrderProduct product) {
        if (product == null || StringUtils.isEmpty(product.getId())) {
            return null;
        }
        return chipsEnglishClazzService.selectChipsEnglishClassByProductId(product.getId());
    }


}
