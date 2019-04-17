package com.voxlearning.utopia.service.ai.impl.service.processor.talkinteracte;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.data.AIQuestionAppraisionRequest;
import com.voxlearning.utopia.service.ai.context.AITalkLessonInteractContext;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Optional;

@Named
public class AUTI_LoadRequest extends AbstractAiSupport implements IAITask<AITalkLessonInteractContext> {

    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(AITalkLessonInteractContext context) {
        switch (context.getType()) {
            case Dialogue:
            case video_conversation:
                if (StringUtils.isNotEmpty(context.getLessonId())) {
                    aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().addRecord(context, true, context.getInput());
                }
                context.setRequestUrl(RuntimeMode.current().gt(Mode.TEST) ? "http://dialogue.17zuoye.com/aiteacher/scene" : "http://10.7.13.75:31001/aiteacher/scene");
                break;
            case Task:
            case task_conversation:
                if (StringUtils.isNotEmpty(context.getLessonId())) {
                    aiCacheSystem.getUserTaskTalkSceneResultCacheManager().addRecord(context, true, context.getInput());
                }
                context.setRequestUrl(RuntimeMode.current().gt(Mode.TEST) ? "http://dialogue.17zuoye.com/aiteacher/task" : "http://10.7.13.75:31001/aiteacher/task");
                context.getRequestParameter().put("name", context.getRoleName());
                break;
            default:
                context.errorResponse("type error.");
                return;
        }

        context.getRequestParameter().put("userid", context.getUsercode());
        context.getRequestParameter().put("input", handleRequestInput(context.getInput()));

    }

    private String handleRequestInput(String input) {
        AIQuestionAppraisionRequest request = JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class);
        String requestStr;
        if (request == null) {
            requestStr = input;
        } else {
            requestStr = Optional.ofNullable(request.getLines())
                    .filter(e -> CollectionUtils.isNotEmpty(e))
                    .map(e -> e.stream().filter(e1 -> e1.getStandardScore() != null && e1.getStandardScore().compareTo(new BigDecimal(chipsContentService.talkInteractLimitScore())) > 0).findFirst().orElse(null))
                    .map(e -> e.getSample())
                    .filter(e -> StringUtils.isNotBlank(e))
                    .orElse("");
        }
        return requestStr;
    }
}
