package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.utopia.service.afenti.api.context.ElfResultContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
@AfentiTasks({
        IER_LoadWrongQuestionLibrary.class,
        IER_GrowthWorldTopic.class,
        IER_GenerateSimilarQuestion.class,
        IER_SimilarQuestionExist.class,
        IER_SimilarQuestionNotFound.class
})
public class IncorrectElfResultProcessor extends AbstractAfentiProcessor<ElfResultContext> {
}
