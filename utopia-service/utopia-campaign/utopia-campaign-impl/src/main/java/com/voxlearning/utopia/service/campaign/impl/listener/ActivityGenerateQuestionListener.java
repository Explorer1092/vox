package com.voxlearning.utopia.service.campaign.impl.listener;


import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.campaign.impl.service.StudentActivityServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.campaign.activity.generate.question"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.campaign.activity.generate.question")
        },
        maxPermits = 4
)
public class ActivityGenerateQuestionListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private StudentActivityServiceImpl studentActivityService;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            return;
        }
        try {
            String activityId = message.getBodyAsString();
            studentActivityService.generateSudokuQuestion(activityId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


}
