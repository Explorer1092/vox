package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Named
public class CWQR_LoadUnit extends AbstractAiSupport implements IAITask<ChipsWechatQuestionResultContext> {

    @Override
    public void execute(ChipsWechatQuestionResultContext context) {
        if (Boolean.FALSE.equals(context.getChipsQuestionResultRequest().getLessonLast())) {
            context.terminateTask();
            return;
        }
        StoneUnitData unitData = Optional.ofNullable(context.getChipsQuestionResultRequest())
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(e.getUnitId())))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> e.get(context.getChipsQuestionResultRequest().getUnitId()))
                .map(StoneUnitData::newInstance)
                .orElse(null);
        if (unitData == null) {
            context.errorResponse("单元不存在");
            context.setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
            return;
        }
        context.setUnit(unitData);

        List<ChipsWechatUserLessonResultHistory> lessonResultHistoryList = chipsWechatUserLessonResultHistoryDao.loadByUnit(context.getUserId(), context.getChipsQuestionResultRequest().getUnitId());
        context.setUnitLessonResultList(lessonResultHistoryList);

        List<ChipsWechatUserQuestionResultHistory> aiUserQuestionResultHistoryList = chipsWechatUserQuestionResultHistoryDao.loadByUnitId(context.getUserId(), context.getChipsQuestionResultRequest().getUnitId());
        context.setUnitQuestionResultList(aiUserQuestionResultHistoryList);
    }
}
