package com.voxlearning.utopia.service.ai.impl.service.processor.drawingtask;

import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsDrawingTaskLoadContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Named
public class CDTL_LoadPopData extends AbstractAiSupport implements IAITask<ChipsDrawingTaskLoadContext> {

    @Override
    public void execute(ChipsDrawingTaskLoadContext context) {
        List<ChipsUserDrawingTask> userDrawingTasks = Optional.ofNullable(context.getUserDrawingTasks()).orElse(Collections.emptyList());
        context.setPopData(chipsUserService.loadDrawingTaskPopData(userDrawingTasks, context.getUserId()));
    }
}
