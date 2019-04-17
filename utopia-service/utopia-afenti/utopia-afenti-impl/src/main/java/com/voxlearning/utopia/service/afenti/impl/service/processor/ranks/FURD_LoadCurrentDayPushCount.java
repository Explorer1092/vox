package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiStats;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Date;

import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * @author ruib
 * @since 16/7/17
 */
@Named
public class FURD_LoadCurrentDayPushCount extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchUnitRanksContext context) {
        String pushDate = DateUtils.dateToString(new Date(), FORMAT_SQL_DATE);
        String value = context.getSubject() == ENGLISH ? pushDate :
                StringUtils.join(Arrays.asList(pushDate, context.getSubject()), "|");
        if (context.getLearningType() == AfentiLearningType.preparation) {
            value = context.getSubject() == ENGLISH ? StringUtils.join(Arrays.asList(pushDate, context.getLearningType().name()), "|") :
                    StringUtils.join(Arrays.asList(pushDate, context.getSubject(), context.getLearningType().name()), "|");
        }
        String finalValue = value;
        UserAfentiStats stats = afentiLoader.loadUserAfentiStats(context.getStudent().getId());
        if (stats == null) return;

        long count = stats.getStats().values().stream().filter(v -> StringUtils.equals(v, finalValue)).count();
        context.setCount(count);
    }
}
