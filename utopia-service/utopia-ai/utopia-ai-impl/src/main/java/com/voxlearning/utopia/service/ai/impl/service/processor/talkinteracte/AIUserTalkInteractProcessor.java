package com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte;

import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.AITask;
import com.voxlearning.utopia.service.ai.impl.service.processor.AbstractAIProcessor;

import javax.inject.Named;

@AITask(value = {
        AUTI_LoadRequest.class,
        AUTI_LoadResponse.class,
        AUTI_SaveCompleteDialogueTalk.class,
        AUTI_UserQuestionResultCollect.class

})
@Named
public class AIUserTalkInteractProcessor extends AbstractAIProcessor<AITalkLessonInteractContext> {

}
