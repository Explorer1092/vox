package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        CWQR_LoadQuestion.class,
        CWQR_LoadLesson.class,
        CWQR_SaveQuestionResult.class,
        CWQR_SaveLessonResult_Dialogue.class,
        CWQR_LoadUnit.class,
        CWQR_SaveUnitResult.class
})
@Named
public class ChipsWechatQuestionResultProcessor extends AbstractAIProcessor<ChipsWechatQuestionResultContext> {

}
