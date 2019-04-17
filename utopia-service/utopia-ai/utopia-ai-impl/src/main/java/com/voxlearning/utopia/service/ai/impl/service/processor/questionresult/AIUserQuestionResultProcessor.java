package com.voxlearning.utopia.service.ai.impl.service.processor.questionresult;

import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        AQP_SaveQuestionResult.class,
        AQP_UpsertLessonResult.class,
        AQP_UpsertDialogueLessonResult.class,
        AQP_UpsertTaskLessonResult.class,
        AQP_UpsertUnitResult.class,
        AQP_SendCoupon.class,
        AQP_SaveBookResult.class
})
@Named
public class AIUserQuestionResultProcessor extends AbstractAIProcessor<AIUserQuestionContext> {
}
