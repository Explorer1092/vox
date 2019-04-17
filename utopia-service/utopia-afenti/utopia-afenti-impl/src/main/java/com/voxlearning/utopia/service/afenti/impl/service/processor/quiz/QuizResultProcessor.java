package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz;

import com.voxlearning.utopia.service.afenti.api.context.QuizResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/12
 */
@Named
@AfentiTasks({
        QR_LoadQuizResult.class,
        QR_UpdateQuizResult.class,
        QR_CalculateOrNot.class,
        QR_CalculateScore.class,
        QR_ParentFairylandRelative.class,
        QR_CaqlculateReward.class,
        QR_AddReward.class,
        QR_LoadReport.class
})
public class QuizResultProcessor extends AbstractAfentiProcessor<QuizResultContext> {
}
