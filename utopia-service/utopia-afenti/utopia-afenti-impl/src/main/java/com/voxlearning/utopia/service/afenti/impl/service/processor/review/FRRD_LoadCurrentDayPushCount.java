package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiStats;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_LoadCurrentDayPushCount extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchReviewRanksContext context) {
        String pushDate = DateUtils.dateToString(new Date(), FORMAT_SQL_DATE);

        String finalValue = StringUtils.join(Arrays.asList(pushDate, context.getSubject(), AfentiLearningType.review.name()), "|");
        UserAfentiStats stats = afentiLoader.loadUserAfentiStats(context.getStudent().getId());
        if (stats == null) return;

        long count = stats.getStats().values().stream().filter(v -> StringUtils.equals(v, finalValue)).count();
        context.setCount(count);
    }
}
