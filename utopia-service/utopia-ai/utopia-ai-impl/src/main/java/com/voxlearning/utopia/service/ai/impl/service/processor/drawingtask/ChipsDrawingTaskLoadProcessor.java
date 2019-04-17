package com.voxlearning.utopia.service.ai.impl.service.processor.drawingtask;

import com.voxlearning.utopia.service.ai.impl.context.ChipsDrawingTaskLoadContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        CDTL_LoadLabel.class,
        CDTL_LoadDrawingTask.class,
        CDTL_LoadPopData.class
})
@Named
public class ChipsDrawingTaskLoadProcessor extends AbstractAIProcessor<ChipsDrawingTaskLoadContext> {
}
