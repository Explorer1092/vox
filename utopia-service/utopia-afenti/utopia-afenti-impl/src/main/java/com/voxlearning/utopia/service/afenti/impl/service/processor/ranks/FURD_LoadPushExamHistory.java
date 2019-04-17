package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FURD_LoadPushExamHistory extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchUnitRanksContext context) {
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), context.getLearningType());
        Long studentId = context.getStudent().getId();
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }

        Set<Integer> pushed = afentiLoader
                .loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .filter(h -> StringUtils.equals(h.getNewUnitId(), context.getUnitId()))
                .map(AfentiLearningPlanPushExamHistory::getRank)
                .collect(Collectors.toSet());

        context.getPushed().addAll(pushed);
    }
}
