package com.voxlearning.utopia.service.afenti.impl.service.processor.quiz.term;

/**
 * @author Ruib
 * @since 2016/10/18
 */

import com.voxlearning.utopia.service.afenti.api.context.FetchTermQuizInfoContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

@Named
@AfentiTasks({
        FTQI_LoadQuizStat.class,
        FTQI_TransformStat.class
})
public class FetchTermQuizInfoProcessor extends AbstractAfentiProcessor<FetchTermQuizInfoContext> {
}