package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.PushReviewQuestionContext;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiStats;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;
import static com.voxlearning.utopia.service.afenti.api.constant.AfentiErrorType.PUSH_REVIEW_QUESTION_OVER_THREE_LIMITATION;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class PRQ_CanPush extends SpringContainerSupport implements IAfentiTask<PushReviewQuestionContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    private static List<Long> whiteList = Arrays.asList(380006774L, 380889511L, 30012L, 30030L);

    @Override
    public void execute(PushReviewQuestionContext context) {
        UserAfentiStats stats = afentiLoader.loadUserAfentiStats(context.getStudent().getId());
        if (stats == null) {
            context.setCount(0);
            return; // 没找到，那就推吧
        }
        // 今天推送了几关
        long count = getPushCount(stats, context.getSubject());

        if (count >= 3 && !whiteList.contains(context.getStudent().getId())) {
            context.setErrorCode(PUSH_REVIEW_QUESTION_OVER_THREE_LIMITATION.getCode());
            context.errorResponse(PUSH_REVIEW_QUESTION_OVER_THREE_LIMITATION.getInfo());
            return;
        }
    }
    private long getPushCount(UserAfentiStats stats, Subject subject) {
        String pushDate = DateUtils.dateToString(new Date(), FORMAT_SQL_DATE);
        String value = StringUtils.join(Arrays.asList(pushDate, subject, AfentiLearningType.review.name()), "|");
        return stats.getStats().values().stream().filter(v -> StringUtils.equals(v, value)).count();
    }
}
