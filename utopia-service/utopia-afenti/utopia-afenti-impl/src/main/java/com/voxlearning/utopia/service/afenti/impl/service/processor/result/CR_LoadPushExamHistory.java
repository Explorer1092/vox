package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
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
 * @since 2016/7/21
 */
@Named
public class CR_LoadPushExamHistory extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(CastleResultContext context) {
        Long studentId = context.getStudent().getId();
        String bookId = context.getBookId();
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }

        List<AfentiLearningPlanPushExamHistory> histories = afentiLoader
                .loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(studentId,
                        AfentiUtils.getBookId(bookId, context.getAfentiLearningType()))
                .stream()
                .filter(h -> StringUtils.equals(h.getNewUnitId(), context.getUnitId()))
                .filter(h -> Objects.equals(h.getRank(), context.getRank()))
                .collect(Collectors.toList());

        context.getHistories().addAll(histories);
    }
}
