package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.utopia.service.afenti.api.context.FetchElfPageContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/10/31
 */
@Named
@AfentiTasks({
        FEP_LoadWrongQuestionLibrary.class,
        FEP_ForWrongAndRescued.class,
        FEP_ForSimilar.class,
        FEP_ForDisableQuestion.class,
        FEP_ClassifyData.class
})
public class FetchElfPageProcessor extends AbstractAfentiProcessor<FetchElfPageContext> {
}
