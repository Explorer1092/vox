package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonResultHistory;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Named
public class CQR_LoadUnit extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Override
    public void execute(ChipsQuestionResultContext context) {
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
            return;
        }
        List<AIUserLessonResultHistory> lessonResultHistoryList =
                aiUserLessonResultHistoryDao.loadByUserIdAndUnitId(context.getUserId(), context.getChipsQuestionResultRequest().getUnitId());
        context.setLessonResultHistoryList(lessonResultHistoryList);
        context.setUnit(unitData);

        List<AIUserQuestionResultHistory> aiUserQuestionResultHistoryList = aiUserQuestionResultHistoryDao.loadByUidAndUnitId(context.getUserId(), context.getChipsQuestionResultRequest().getUnitId());
        context.setUnitQuestionResultList(aiUserQuestionResultHistoryList);
    }
}
