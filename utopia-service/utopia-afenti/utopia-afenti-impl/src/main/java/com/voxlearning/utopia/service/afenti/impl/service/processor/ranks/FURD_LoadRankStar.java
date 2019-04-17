package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FURD_LoadRankStar extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchUnitRanksContext context) {
        Long userId = context.getStudent().getId();
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), context.getLearningType());
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }
        List<AfentiLearningPlanUserRankStat> stats = afentiLoader
                .loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(userId, bookId)
                .stream()
                .filter(s -> StringUtils.equals(s.getNewUnitId(), context.getUnitId()))
                .collect(Collectors.toList());

        for (AfentiLearningPlanUserRankStat stat : stats) {
            context.getRank_star_map().put(stat.getRank(), stat.getStar());
        }
    }
}
