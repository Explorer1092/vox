package com.voxlearning.utopia.service.ai.impl.service.processor.v2.wechatresult;

import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserQuestionResultHistory;
import com.voxlearning.utopia.service.ai.impl.AbstractAiSupport;
import com.voxlearning.utopia.service.ai.impl.context.ChipsWechatQuestionResultContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.IAITask;

import javax.inject.Named;

@Named
public class CWQR_SaveQuestionResult extends AbstractAiSupport implements IAITask<ChipsWechatQuestionResultContext> {

    @Override
    public void execute(ChipsWechatQuestionResultContext context) {
        if (context.getQuestionType().isPersistentResult()) {
            ChipsWechatUserQuestionResultHistory history = ChipsWechatUserQuestionResultHistory.translate(context.getChipsQuestionResultRequest(), context.getQuestionType(),
                context.getUserId(), context.getLesson().getJsonData().getLesson_type());
            chipsWechatUserQuestionResultHistoryDao.loadByLessonId(context.getUserId(), context.getChipsQuestionResultRequest().getLessonId())
                    .stream()
                    .filter(e -> e.getQid().equals(context.getChipsQuestionResultRequest().getQid()))
                    .forEach(e -> chipsWechatUserQuestionResultHistoryDao.disabled(e));
            chipsWechatUserQuestionResultHistoryDao.insert(history);
        }
        
        if (!context.getChipsQuestionResultRequest().getLessonLast()) { //不是最后一道题则停止
            context.terminateTask();
        }
    }
}
