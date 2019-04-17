package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.unit;

import com.voxlearning.utopia.service.afenti.api.context.FetchQuizInfoContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/10/13
 */
@Named
@AfentiTasks({
        FQI_LoadAfentiBook.class,
        FQI_QuizExistenceCheck.class,
        FQI_QuizAccomplishmentCheck.class
})
public class FetchQuizInfoProcessor extends AbstractAfentiProcessor<FetchQuizInfoContext> {
}
