package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushReviewQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.dao.AfentiLearningPlanPushExamHistoryDao;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class PRQ_SavePushResult extends SpringContainerSupport implements IAfentiTask<PushReviewQuestionContext> {
    @Inject
    private AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;

    @Override
    public void execute(PushReviewQuestionContext context) {
        for (NewQuestion question : context.getReviewQuestions()) {
            AfentiLearningPlanPushExamHistory history = new AfentiLearningPlanPushExamHistory();
            history.setUserId(context.getStudent().getId());
            history.setNewBookId(AfentiUtils.getBookId(context.getBookId(), AfentiLearningType.review));
            history.setNewUnitId(context.getUnitId());
            history.setSubject(context.getSubject().name());
            history.setRank(1);
            history.setKnowledgePoint(CollectionUtils.isEmpty(question.getKnowledgePointsNew()) ? "" : question.getKnowledgePointsNew().get(0).getId());
            history.setExamId(question.getId());
            history.setPattern(question.getContentTypeId() == null ? "" : question.getContentTypeId().toString());
            history.setCreatetime(new Date());
            history.setUpdatetime(new Date());
            history.setRightNum(0);
            history.setErrorNum(0);
            afentiLearningPlanPushExamHistoryDao.insert(history);
            context.getHistories().add(history);
        }
    }
}
