package com.voxlearning.utopia.service.ai.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.ChipsRank;

import java.util.List;

public class UserInvitationRankCache {

    private static String RANK_CACHE_KEY = "CHIPS_INVITATION_RANK";

    private static class RankCacheHolder {

        private static final UtopiaCache persistent;

        static {
            persistent = CacheSystem.CBS.getCache("persistence");
        }
    }

    public static void save(List<ChipsRank> chipsRanks) {
        UtopiaCache persistence = RankCacheHolder.persistent;
        //只存一天的
        persistence.add(RANK_CACHE_KEY, DateUtils.getCurrentToDayEndSecond(), chipsRanks);
    }


    public static List<ChipsRank> load() {
        UtopiaCache persistence = RankCacheHolder.persistent;
        return persistence.load(RANK_CACHE_KEY);
    }
}
