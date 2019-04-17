package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class RR_LoadUserRankStat extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(ReviewResultContext context) {
        Long studentId = context.getStudent().getId();
        String bookId = AfentiUtils.getBookId(context.getBookId(), AfentiLearningType.review);

        AfentiLearningPlanUserRankStat stat = afentiLoader
                .loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .filter(s -> StringUtils.equals(s.getNewUnitId(), context.getUnitId()))
                .findFirst()
                .orElse(null);

        context.setStat(stat);
    }
}
