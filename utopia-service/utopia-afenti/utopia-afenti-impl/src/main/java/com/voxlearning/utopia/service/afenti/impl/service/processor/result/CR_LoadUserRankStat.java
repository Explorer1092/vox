package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * @author Ruib
 * @since 2016/7/21
 */
@Named
public class CR_LoadUserRankStat extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(CastleResultContext context) {
        Long studentId = context.getStudent().getId();
        String bookId = AfentiUtils.getBookId(context.getBookId(), context.getAfentiLearningType());
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }

        AfentiLearningPlanUserRankStat stat = afentiLoader
                .loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(studentId, bookId)
                .stream()
                .filter(s -> StringUtils.equals(s.getNewUnitId(), context.getUnitId()))
                .filter(s -> Objects.equals(s.getRank(), context.getRank()))
                .findFirst()
                .orElse(null);

        context.setStat(stat);
    }
}
