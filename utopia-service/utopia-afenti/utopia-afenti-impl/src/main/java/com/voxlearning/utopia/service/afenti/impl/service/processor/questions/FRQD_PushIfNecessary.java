package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FRQD_PushIfNecessary extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject private PushQuestionProcessor processor;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        List<AfentiLearningPlanPushExamHistory> histories = context.getHistories();
        if (CollectionUtils.isNotEmpty(histories)) return;

        try {
            PushQuestionContext ctx = processor.process(new PushQuestionContext(context.getStudent().getId(),
                    context.getSubject(), context.getBook().book.getId(), context.getUnitId(), context.getSectionId(),
                    context.getRank(), context.isPaid(), UtopiaAfentiConstants.isUltimateUnit(context.getUnitId()),
                    context.getLearningType(), context.getIsNewRankBook()));

            if (ctx.isSuccessful()) {
                context.getHistories().addAll(ctx.getHistories());
            } else {
                context.errorResponse(ctx.getMessage());
                context.setErrorCode(ctx.getErrorCode());
            }
        } catch (Exception ex) {
            logger.error("FRQD_PushIfNecessary push question failed.", ex);
            context.errorResponse();
        }
    }
}
