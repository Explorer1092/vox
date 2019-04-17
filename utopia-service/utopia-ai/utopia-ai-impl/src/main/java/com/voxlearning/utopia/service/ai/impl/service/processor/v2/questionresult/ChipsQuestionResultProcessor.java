package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        CQR_LoadQuestion.class,
        CQR_LoadLesson.class,
        CQR_SaveQuestionResult.class,
        CQR_SaveLessonResult_Common.class,
        CQR_SaveLessonResult_Dialogue.class,
        CQR_SaveLessonResult_Task.class,
        CQR_LoadUnit.class,
        CQR_SaveUnitResult.class,
        CQR_GenDrawingTask.class,
        CRQ_UpsertActiveServiceRecord.class,
        CQR_SendCoupon.class,
        CQR_SaveBookResult.class
})
@Named
public class ChipsQuestionResultProcessor extends AbstractAIProcessor<ChipsQuestionResultContext> {

}
