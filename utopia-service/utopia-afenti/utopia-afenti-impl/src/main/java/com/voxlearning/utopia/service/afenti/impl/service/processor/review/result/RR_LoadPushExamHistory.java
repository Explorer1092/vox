package com.voxlearning.utopia.service.afenti.impl.service.processor.review.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.ReviewResultContext;
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
public class RR_LoadPushExamHistory extends SpringContainerSupport implements IAfentiTask<ReviewResultContext> {
    @Inject
    private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(ReviewResultContext context) {
        Long studentId = context.getStudent().getId();
        String bookId = context.getBookId();

        List<AfentiLearningPlanPushExamHistory> histories = afentiLoader
                .loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(studentId, AfentiUtils.getBookId(bookId, AfentiLearningType.review))
                .stream()
                .filter(h -> StringUtils.equals(h.getNewUnitId(), context.getUnitId()))
                .collect(Collectors.toList());

        context.getHistories().addAll(histories);
    }
}
