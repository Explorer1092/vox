package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/19
 */
@Named
@AfentiTasks({
        FTQQ_LoadQuizResult.class,
        FTQQ_InitQuizResultIfNecessary.class,
        FTQQ_TransformQuizQuestion.class
})
public class FetchTermQuizQuestionProcessor extends AbstractAfentiProcessor<FetchTermQuizQuestionContext> {
}
