package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_LoadRankStar extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchReviewRanksContext context) {
        Long userId = context.getStudent().getId();
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), AfentiLearningType.review);
        List<AfentiLearningPlanUserRankStat> stats = afentiLoader.loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(userId, bookId);

        for (AfentiLearningPlanUserRankStat stat : stats) {
            context.getRank_star_map().put(stat.getNewUnitId(), stat.getStar());
        }
    }
}
