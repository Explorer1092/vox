package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.utopia.service.afenti.api.context.TermQuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/12/20
 */
@Named
@AfentiTasks({
        TQR_LoadQuizResult.class,
        TQR_UpdateQuizResult.class,
        TQR_RecordWrongQuestionLibrary.class,
        TQR_CalculateOrNot.class,
        TQR_CalculateScore.class,
        TQR_CaqlculateReward.class,
        TQR_AddReward.class
})
public class TermQuizResultProcessor extends AbstractAfentiProcessor<TermQuizResultContext> {
}
