package com.voxlearning.utopia.service.afenti.impl.service.processor.units;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.context.FetchBookUnitsContext;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Named
public class FBU_LoadUserRankStat extends SpringContainerSupport implements IAfentiTask<FetchBookUnitsContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchBookUnitsContext context) {
        Long userId = context.getStudent().getId();
        String bookId = AfentiUtils.getBookId(context.getBook().book.getId(), context.getLearningType());
        if (context.getLearningType() == AfentiLearningType.castle && context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }

        Map<String, List<AfentiLearningPlanUserRankStat>> map = afentiLoader
                .loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(userId, bookId)
                .stream()
                .filter(r -> StringUtils.isNotBlank(r.getNewUnitId()))
                .collect(Collectors.groupingBy(AfentiLearningPlanUserRankStat::getNewUnitId));

        for (String unitId : map.keySet()) {
            context.getUnit_asrc_map().put(unitId, map.get(unitId).size());
            context.getUnit_asc_map().put(unitId, map.get(unitId).stream()
                    .mapToInt(AfentiLearningPlanUserRankStat::getStar).sum());
        }
    }
}
