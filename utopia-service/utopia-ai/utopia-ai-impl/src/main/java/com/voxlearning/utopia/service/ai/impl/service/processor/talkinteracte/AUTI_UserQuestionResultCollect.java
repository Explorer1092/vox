package com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte;

import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;

@Named
public class AUTI_UserQuestionResultCollect extends AbstractAiSupport implements IAITask<AITalkLessonInteractContext> {

    @Override
    public void execute(AITalkLessonInteractContext context) {

        switch (context.getType()) {
            case Dialogue:
            case Task:
            case task_conversation:
            case video_conversation:
                // Collect result
                aiUserQuestionResultCollectionQueueProducer.processCollect(context);
                break;
        }



    }
}
