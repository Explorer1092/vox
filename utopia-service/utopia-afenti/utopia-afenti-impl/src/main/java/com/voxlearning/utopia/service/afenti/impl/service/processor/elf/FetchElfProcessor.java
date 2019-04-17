package com.voxlearning.utopia.service.afenti.impl.service.processor.elf;

import com.voxlearning.utopia.service.afenti.api.context.FetchElfContext;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AbstractAfentiProcessor;
import com.voxlearning.utopia.service.afenti.impl.service.processor.AfentiTasks;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2016/7/24
 */
@Named
@AfentiTasks({
        FE_LoadWrongQuestionLibrary.class,
        FE_ClassifyData.class
})
public class FetchElfProcessor extends AbstractAfentiProcessor<FetchElfContext> {
}
