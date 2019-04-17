package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushReviewQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiStatsPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class PRQ_RecordPushAction extends SpringContainerSupport implements IAfentiTask<PushReviewQuestionContext> {
    @Inject
    private UserAfentiStatsPersistence userAfentiStatsPersistence;

    @Override
    public void execute(PushReviewQuestionContext context) {
        String bookId = AfentiUtils.getBookId(context.getBookId(), AfentiLearningType.review);
        String key = StringUtils.join(Arrays.asList(bookId, context.getUnitId(), 1), "_");
        String value = StringUtils.join(Arrays.asList(DateUtils.dateToString(new Date(),
                DateUtils.FORMAT_SQL_DATE), context.getSubject(), AfentiLearningType.review.name()), "|");
        try {
            userAfentiStatsPersistence.updateStats(context.getStudent().getId(), key, value);
        } catch (Exception ex) {
            logger.error("PRQ_RecordPushAction error. User {}, book {}, unit {}",
                    context.getStudent().getId(), context.getBookId(), context.getUnitId(),  ex);
        }
    }
}
