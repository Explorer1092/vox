package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushQuestionContext;
import com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiStatsPersistence;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Ruib
 * @since 2016/7/18
 */
@Named
public class PQ_RecordPush extends SpringContainerSupport implements IAfentiTask<PushQuestionContext> {
    @Inject private UserAfentiStatsPersistence userAfentiStatsPersistence;

    @Override
    public void execute(PushQuestionContext context) {
        String bookId = AfentiUtils.getBookId(context.getBookId(), context.getLearningType());
        if (context.getIsNewRankBook()) {
            bookId = AfentiUtils.getNewBookId(bookId);
        }
        String key = StringUtils.join(Arrays.asList(bookId, context.getUnitId(), context.getRank()), "_");
        String value;
        if (context.getLearningType() == AfentiLearningType.castle) {
            if (Subject.ENGLISH == context.getSubject()) {
                value = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
            } else {
                value = StringUtils.join(Arrays.asList(DateUtils.dateToString(new Date(),
                        DateUtils.FORMAT_SQL_DATE), context.getSubject()), "|");
            }
        } else {
            if (Subject.ENGLISH == context.getSubject()) {
                value = StringUtils.join(Arrays.asList(DateUtils.dateToString(new Date(),
                        DateUtils.FORMAT_SQL_DATE), context.getLearningType().name()), "|");
            } else {
                value = StringUtils.join(Arrays.asList(DateUtils.dateToString(new Date(),
                        DateUtils.FORMAT_SQL_DATE), context.getSubject(), context.getLearningType().name()), "|");
            }
        }

        try {
            userAfentiStatsPersistence.updateStats(context.getStudentId(), key, value);
        } catch (Exception ex) {
            logger.error("PQ_RecordPush error. User {}, book {}, unit {}, rank {}, learningType {}",
                    context.getStudentId(), context.getBookId(), context.getUnitId(), context.getRank(), context.getLearningType(), ex);
        }
    }
}
