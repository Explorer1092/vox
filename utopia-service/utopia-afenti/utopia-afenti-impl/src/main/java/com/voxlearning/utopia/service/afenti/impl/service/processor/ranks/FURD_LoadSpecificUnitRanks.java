package com.voxlearning.utopia.service.afenti.impl.service.processor.ranks;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.data.RankType;
import com.voxlearning.utopia.service.afenti.api.constant.UnitRankType;
import com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants;
import com.voxlearning.utopia.service.afenti.api.context.FetchUnitRanksContext;
import com.voxlearning.utopia.service.afenti.api.data.UnitRank;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ruib
 * @since 2016/7/15
 */
@Named
public class FURD_LoadSpecificUnitRanks extends SpringContainerSupport implements IAfentiTask<FetchUnitRanksContext> {

    @Override
    public void execute(FetchUnitRanksContext context) {
        if (context.getUnitRankType() == UnitRankType.COMMON) {
            return;
        }
        Map<Integer, Integer> rank_star_map = context.getRank_star_map();
        Map<Integer, List<Map<String, Object>>> rank_footprint_map = context.getRank_footprint_map();
        Set<Integer> pushed = context.getPushed();

        int rankCount = 0;
        if (context.getUnitRankType() == UnitRankType.ULTIMATE) {
            rankCount = UtopiaAfentiConstants.ULTIMATE_RANK;
        }
        if (context.getUnitRankType() == UnitRankType.MIDTERM) {
            rankCount = UtopiaAfentiConstants.MIDTERM_RANK;
        }
        if (context.getUnitRankType() == UnitRankType.TERMINAL) {
            rankCount = UtopiaAfentiConstants.TERMINAL_RANK;
        }

        for (int i = 1; i <= rankCount; i++) {
            UnitRank unitRank = new UnitRank();
            unitRank.rank = i;
            unitRank.rankType = RankType.BASE;
            unitRank.star = rank_star_map.containsKey(i) ? rank_star_map.get(i) : 0;
            unitRank.footprints = rank_footprint_map.containsKey(i) ? rank_footprint_map.get(i) : new ArrayList<>();
            unitRank.pushed = pushed.contains(i);
            context.getRanks().add(unitRank);
        }
    }
}
