package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer on 2017/8/15.
 */
@Named
public class FURD_LoadNewRank extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchUnitRanksContext context) {
        if (context.getLearningType() == AfentiLearningType.preparation) {
            return;
        }
        List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                .loadAfentiLearningPlanUnitRankManagerByNewBookId(AfentiUtils.getNewBookId(context.getBook().book.getId()));
        if (CollectionUtils.isNotEmpty(ranks)) {
            context.setIsNewRankBook(true);
            context.setNewRankBookId(AfentiUtils.getNewBookId(context.getBook().book.getId()));
        }
    }
}
