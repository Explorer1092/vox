package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.data.DrawingTabConfig;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsUserDrawingTask;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;

@Named
public class CQR_GenDrawingTask extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Override
    public void execute(ChipsQuestionResultContext context) {
        if (StringUtils.isBlank(context.getUnit().getJsonData().getReward_illust_id())) {
            return;
        }

        DrawingTabConfig tabConfig = chipsContentService.loadUserDrawingTab(context.getUserId()).stream()
                .filter(e -> e.getBooks().contains(context.getChipsQuestionResultRequest().getBookId()))
                .findFirst().orElse(null);
        if (tabConfig == null) {
            return;
        }

        AIUserUnitResultHistory unitResultHistory = aiUserUnitResultHistoryDao.load(context.getUserId(), context.getUnit().getId());
        if (unitResultHistory == null || unitResultHistory.getDrawingTaskId() != null) {
            return;
        }
        ChipsUserDrawingTask task = chipsUserDrawingTaskPersistence.loadByUser(context.getUserId()).stream().filter(e -> e.getUnitId().equals(context.getUnit().getId())).findFirst().orElse(null);

        if (task == null || !task.getDrawingId().equals(context.getUnit().getJsonData().getReward_illust_id())) {
            chipsUserDrawingTaskPersistence.insertOrUpdate(context.getUserId(), context.getChipsQuestionResultRequest().getBookId(),
                    context.getUnit().getId(), context.getUnit().getJsonData().getReward_illust_id());
            task = chipsUserDrawingTaskPersistence.loadByUser(context.getUserId()).stream().filter(e -> e.getUnitId().equals(context.getUnit().getId())).findFirst().orElse(null);
        }

        if (task != null) {
            unitResultHistory.setDrawingTaskId(task.getId());
            aiUserUnitResultHistoryDao.upsert(unitResultHistory);
        }
    }
}
