package com.voxlearning.utopia.service.ai.impl.service.processor.v2.questionresult;

import com.voxlearning.utopia.service.ai.entity.AIUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;

@Named
public class CQR_SaveQuestionResult extends AbstractAiSupport implements IAITask<ChipsQuestionResultContext> {

    @Override
    public void execute(ChipsQuestionResultContext context) {
        if (context.getQuestionType().isPersistentResult()) {
            AIUserQuestionResultHistory history = AIUserQuestionResultHistory.translate(context.getChipsQuestionResultRequest(), context.getQuestionType(),
                context.getUserId(), context.getLesson().getJsonData().getLesson_type());
            aiUserQuestionResultHistoryDao.loadByUidAndLessonId(context.getUserId(), context.getChipsQuestionResultRequest().getLessonId())
                    .stream()
                    .filter(e -> e.getQid().equals(context.getChipsQuestionResultRequest().getQid()))
                    .forEach(e -> aiUserQuestionResultHistoryDao.disableOld(e));
            aiUserQuestionResultHistoryDao.insert(history);
        }
        
        if (!context.getChipsQuestionResultRequest().getLessonLast()) { //不是最后一道题则停止
            context.terminateTask();
        }
    }
}
