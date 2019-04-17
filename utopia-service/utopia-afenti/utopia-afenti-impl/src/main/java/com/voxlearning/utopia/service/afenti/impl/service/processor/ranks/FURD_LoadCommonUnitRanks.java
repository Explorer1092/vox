package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.data.UnitRank;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.impl.service.AfentiLoaderImpl;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FURD_LoadCommonUnitRanks extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {
    @Inject private AfentiLoaderImpl afentiLoader;

    @Override
    public void execute(FetchUnitRanksContext context) {
        if (context.getUnitRankType() != UnitRankType.COMMON) {
            return;
        }
        String bookId = context.getBook().book.getId();
        if (context.getIsNewRankBook()) {
            bookId = context.getNewRankBookId();
        }
        Map<Integer, Integer> rank_star_map = context.getRank_star_map();
        Map<Integer, List<Map<String, Object>>> rank_footprint_map = context.getRank_footprint_map();
        Set<Integer> pushed = context.getPushed();

        // 获取当前单元所有关卡
        List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader
                .loadAfentiLearningPlanUnitRankManagerByNewBookId(bookId)
                .stream()
                .filter(r -> r.getType() == context.getLearningType())
                .filter(r -> StringUtils.equals(r.getNewUnitId(), context.getUnitId()))
                .collect(Collectors.toList());

        for (AfentiLearningPlanUnitRankManager rank : ranks) {
            UnitRank unitRank = new UnitRank();
            unitRank.rank = rank.getRank();
            unitRank.rankType = rank.fetchRankType();
            unitRank.star = rank_star_map.containsKey(rank.getRank()) ? rank_star_map.get(rank.getRank()) : 0;
            unitRank.footprints = rank_footprint_map.containsKey(rank.getRank()) ? rank_footprint_map.get(rank.getRank())
                    : new ArrayList<>();
            unitRank.pushed = pushed.contains(rank.getRank());
            context.getRanks().add(unitRank);
        }
    }
}
