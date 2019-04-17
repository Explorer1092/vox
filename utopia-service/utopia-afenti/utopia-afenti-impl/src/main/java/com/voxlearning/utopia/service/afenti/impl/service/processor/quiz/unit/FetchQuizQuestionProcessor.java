package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.utopia.service.afenti.api.context.FetchQuizQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/14
 */
@Named
@AfentiTasks({
        FQQ_LoadAfentiBook.class,
        FQQ_LoadQuizQuestions.class,
        FQQ_InitAfentiQuizResult.class,
        FQQ_TransformQuizQuestion.class
})
public class FetchQuizQuestionProcessor extends AbstractAfentiProcessor<FetchQuizQuestionContext> {
}
