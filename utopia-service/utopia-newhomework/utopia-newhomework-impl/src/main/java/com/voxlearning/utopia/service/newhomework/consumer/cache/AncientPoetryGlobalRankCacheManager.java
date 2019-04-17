package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.List;
import java.util.Map;

@UtopiaCacheExpiration(2 * 24 * 3600)
public class AncientPoetryGlobalRankCacheManager extends PojoCacheObject<String, List<Map<String, Object>>> {

    public AncientPoetryGlobalRankCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getSchoolGlobalRankCacheKey(Long schoolId, Integer clazzLevel) {
        return "ANCIENT_POETRY_SCHOOL_GLOBAL_RANK_" + schoolId + "_" + clazzLevel;
    }

    public String getSchoolStudentRankCacheKey(Long schoolId, Integer clazzLevel, Long studentId) {
        return "ANCIENT_POETRY_SCHOOL_STUDENT_RANK_" + schoolId + "_" + clazzLevel + "_" + studentId;
    }

    public String getRegionGlobalRankCacheKey(Integer regionId, Integer clazzLevel) {
        return "ANCIENT_POETRY_REGION_GLOBAL_RANK_" + regionId + "_" + clazzLevel;
    }

    public String getRegionStudentRankCacheKey(Integer regionId, Integer clazzLevel, Long studentId) {
        return "ANCIENT_POETRY_REGION_STUDENT_RANK_" + regionId + "_" + clazzLevel + "_" + studentId;
    }
}