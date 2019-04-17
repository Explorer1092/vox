package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FRQD_LoadPushExamHistory extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), context.getLearningType());
        if (context.getLearningType() == AfentiLearningType.castle && context.getIsNewRankBook()) {
            bookId = AfentiUtils.getNewBookId(context.getBook().book.getId());
        }
        List<AfentiLearningPlanPushExamHistory> histories = afentiLoader
                .loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(context.getStudent().getId(), bookId)
                .stream()
                .filter(h -> StringUtils.equals(h.getNewUnitId(), context.getUnitId()))
                .filter(h -> Objects.equals(h.getRank(), context.getRank()))
                .collect(Collectors.toList());

        context.getHistories().addAll(histories);
    }
}
