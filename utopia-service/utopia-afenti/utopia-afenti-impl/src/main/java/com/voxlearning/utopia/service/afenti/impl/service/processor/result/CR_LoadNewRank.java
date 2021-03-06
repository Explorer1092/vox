package com.voxlearning.utopia.service.afenti.impl.service.processor.result;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.CastleResultContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer on 2017/8/18.
 */
@Named
public class CR_LoadNewRank extends SpringContainerSupport implements IAfentiTask<CastleResultContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(CastleResultContext context) {
        if (context.getAfentiLearningType() == AfentiLearningType.preparation) {
            return;
        }
        List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                .loadAfentiLearningPlanUnitRankManagerByNewBookId(AfentiUtils.getNewBookId(context.getBookId()));
        if (CollectionUtils.isNotEmpty(ranks)) {
            context.setIsNewRankBook(true);
            context.setNewRankBookId(AfentiUtils.getNewBookId(context.getBookId()));
        }
    }
}
