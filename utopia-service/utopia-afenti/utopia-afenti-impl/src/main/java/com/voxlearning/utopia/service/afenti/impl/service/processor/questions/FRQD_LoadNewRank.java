package com.voxlearning.utopia.service.afenti.impl.service.processor.questions;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchRankQuestionsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Summer on 2017/8/17.
 */
@Named
public class FRQD_LoadNewRank extends SpringContainerSupport implements IAfentiTask<FetchRankQuestionsContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchRankQuestionsContext context) {
        if (context.getLearningType() == AfentiLearningType.preparation) {
            return;
        }
        List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                .loadAfentiLearningPlanUnitRankManagerByNewBookId(AfentiUtils.getNewBookId(context.getBook().book.getId()));
        if (CollectionUtils.isNotEmpty(ranks)) {
            context.setIsNewRankBook(true);
            context.setNewRankBookId(AfentiUtils.getNewBookId(context.getBook().book.getId()));
            List<AfentiLearningPlanUnitRankManager> unitRanks = ranks.stream()
                    .filter(r -> StringUtils.equals(r.getNewUnitId(), context.getUnitId()))
                    .collect(Collectors.toList());
            context.setUnitRanks(unitRanks);
            if (UtopiaAfentiConstants.getUnitType(context.getUnitId()) == UnitRankType.COMMON) {
                AfentiLearningPlanUnitRankManager currentRank = unitRanks.stream()
                        .filter(r -> Objects.equals(r.getRank(), context.getRank())).findFirst().orElse(null);
                if (currentRank == null) {
                    logger.error("afenti load question error, can not find rank, user {}, book {}, unit {}, rank {}",
                            context.getStudent().getId(), context.getBook().book.getId(), context.getUnitId(), context.getRank());
                    return;
                }
                context.setSectionId(currentRank.getNewSectionId());
            }
        }
    }
}
