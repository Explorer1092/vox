package com.voxlearning.utopia.service.afenti.impl.service.processor.review.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchReviewQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author songtao
 * @since 2017/11/29
 */
@Named
public class FRRQD_LoadPushExamHistory extends SpringContainerSupport implements IAfentiTask<FetchReviewQuestionsContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchReviewQuestionsContext context) {
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), AfentiLearningType.review);
        List<AfentiLearningPlanPushExamHistory> histories = afentiLoader
                .loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(context.getStudent().getId(), bookId)
                .stream()
                .filter(h -> StringUtils.equals(h.getNewUnitId(), context.getUnitId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(histories)) {
            context.getHistories().addAll(histories);
        }
    }
}
