package com.voxlearning.utopia.service.ai.impl.service.processor.questionresult;

import com.voxlearning.utopia.service.ai.cache.manager.AICacheSystem;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * Created by Summer on 2018/3/29
 */
@Named
public class AQP_SaveQuestionResult extends AbstractAiSupport implements IAITask<AIUserQuestionContext> {
    @Inject
    private AICacheSystem aiCacheSystem;

    @Override
    public void execute(AIUserQuestionContext context) {
        AIUserQuestionResultHistory history =
                AIUserQuestionResultHistory.translate(context.getAiUserQuestionResultRequest());
        history.setUserId(context.getUser().getId());
        if (context.getAiUserQuestionResultRequest().getLessonType() == LessonType.Dialogue) {
            String sessionId = aiCacheSystem.getUserDialogueTalkSceneResultCacheManager().getSessonId(context.getUser().getId(), context.getAiUserQuestionResultRequest().getLessonId());
            aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUser().getId(), history.getLessonId()).stream()
                    .filter(e -> history.getQid().equals(e.getQid()))
                    .filter(e -> !e.getId().equals(sessionId))
                    .forEach(e -> {
                        aiUserQuestionResultHistoryDao.disableOld(e);
                    });
            history.setId(sessionId);
            aiUserQuestionResultHistoryDao.upsertByUser(context.getUser().getId(), history);
            context.setSessionId(sessionId);
        } else {
            AIUserQuestionResultHistory oldHis =
                    aiUserQuestionResultHistoryDao.loadByUidAndQid(context.getUser().getId(), history.getQid());
            if (oldHis != null) {
                aiUserQuestionResultHistoryDao.disableOld(oldHis);
            }
            aiUserQuestionResultHistoryDao.insert(history);
        }

        if (!context.getAiUserQuestionResultRequest().getLessonLast()) { //不是最后一道题则停止
            context.terminateTask();
        }
    }
}
