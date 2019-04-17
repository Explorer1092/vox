package com.voxlearning.utopia.service.afenti.impl.service.processor.review;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2017/11/28
 */
@Named
public class FRRD_LoadPushExamHistory extends SpringContainerSupport implements IAfentiTask<FetchReviewRanksContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchReviewRanksContext context) {
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), AfentiLearningType.review);
        Long studentId = context.getStudent().getId();

        Set<String> pushed = afentiLoader
                .loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .map(AfentiLearningPlanPushExamHistory::getNewUnitId)
                .collect(Collectors.toSet());

        context.getPushed().addAll(pushed);
    }
}
