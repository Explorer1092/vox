package com.voxlearning.utopia.service.ai.impl.service.processor.v2.dailyclass;

import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsContentDailyClassContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
public class CCDCR_LoadDrawingPopData extends AbstractAiSupport implements IAITask<ChipsContentDailyClassContext> {

    @Override
    public void execute(ChipsContentDailyClassContext context) {
        List<ChipsUserDrawingTask> userDrawingTasks = chipsUserDrawingTaskPersistence.loadByUser(context.getUser().getId());

        List<Map<String, Object>> popData = chipsUserService.loadDrawingTaskPopData(userDrawingTasks, context.getUser().getId());

        context.getExtMap().put("popDrawingData", popData);
    }
}
