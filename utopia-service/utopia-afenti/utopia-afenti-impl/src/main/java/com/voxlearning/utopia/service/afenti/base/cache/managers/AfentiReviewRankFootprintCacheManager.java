package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 缓存期末复习闯关中班级同学闯关信息
 * @author songtao
 * @since 2017/11/30
 */
public class AfentiReviewRankFootprintCacheManager extends CacheBaseManager<AfentiReviewRankFootprintCacheManager.GenerateKey, Set<Long>, Long> {

    public AfentiReviewRankFootprintCacheManager(UtopiaCache cache) {
        super(cache);
    }

    @Override
    public int expirationInSeconds() {
        return 86400 * 30 * 2;
    }

    public boolean addRecord(StudentDetail studentDetail, String unitId) {
        String cacheKey = cacheKey(new AfentiReviewRankFootprintCacheManager.GenerateKey(studentDetail.getClazzId(), unitId));
        return casAddSet(cacheKey, studentDetail.getId());
    }

    public Set<Long> loadRecord(Long clazzId, String unitId) {
        String cacheKey = cacheKey(new AfentiReviewRankFootprintCacheManager.GenerateKey(clazzId, unitId));
        return getCache().load(cacheKey);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"clazzId", "unitId"})
    class GenerateKey {
        private Long clazzId;
        private String unitId;

        @Override
        public String toString() {
            return "CLAZZ_ID=" + clazzId + ",UNIT_ID=" + unitId;
        }
    }
}
