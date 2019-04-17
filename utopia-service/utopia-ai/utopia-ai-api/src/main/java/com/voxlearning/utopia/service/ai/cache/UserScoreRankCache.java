package com.voxlearning.utopia.service.ai.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.data.ChipsRank;

import java.util.List;

public class UserScoreRankCache {

    private static String RANK_CACHE_KEY = "CHIPS_USER_SCORE_RANK_";
    private static int TIME = 60 * 60 * 24 * 12;

    private static class RankCacheHolder {

        private static final UtopiaCache persistent;

        static {
            persistent = CacheSystem.CBS.getCache("persistence");
        }
    }

    public static void save(List<ChipsRank> chipsRanks, String clazz, String unitId) {
        UtopiaCache persistence = RankCacheHolder.persistent;
        persistence.delete(RANK_CACHE_KEY + clazz + unitId);
        persistence.set(RANK_CACHE_KEY + clazz + unitId, TIME, chipsRanks);
    }


    public static List<ChipsRank> load(String clazz, String unitId) {
        UtopiaCache persistence = RankCacheHolder.persistent;
        return persistence.load(RANK_CACHE_KEY + clazz + unitId);
    }
}
