package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.utopia.service.afenti.api.context.FetchElfIndexContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/09/20
 */
@Named
@AfentiTasks({
        FEI_LoadWrongQuestionLibrary.class,
        FEI_CountIncorrect.class,
        FEI_CountSimilar.class,
        FEI_CountRescued.class
})
public class FetchElfIndexProcessor extends AbstractAfentiProcessor<FetchElfIndexContext> {

}
