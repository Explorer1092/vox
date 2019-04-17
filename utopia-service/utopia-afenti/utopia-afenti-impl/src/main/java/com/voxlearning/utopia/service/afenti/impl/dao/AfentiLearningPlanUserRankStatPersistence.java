package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.annotation.common.Spring;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.jdbc.persistence.ShardPersistence;
import com.voxlearning.alps.spi.cache.ChangeCacheObject;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@Spring
@Named
@UtopiaCacheSupport(AfentiLearningPlanUserRankStat.class)
public class AfentiLearningPlanUserRankStatPersistence extends
        ShardPersistence<AfentiLearningPlanUserRankStatDao, AfentiLearningPlanUserRankStat, Long> {

    @Override
    protected void calculateCacheDimensions(AfentiLearningPlanUserRankStat source, Collection<String> dimensions) {
        dimensions.add(AfentiLearningPlanUserRankStat.ck_uid_nbid(source.getUserId(), source.getNewBookId()));
        dimensions.add(AfentiLearningPlanUserRankStat.ck_uid_s(source.getUserId(), source.getSubject()));
        dimensions.add(AfentiLearningPlanUserRankStat.ck_iuid(source.getUserId()));
    }

    @Override
    public void persistIntoDatabase(AfentiLearningPlanUserRankStat entity) {
        Long id = dao.insert(entity);
        entity.setId(id);
    }

    @Override
    protected void afterPersistIntoDatabase(AfentiLearningPlanUserRankStat entity) {
        String key1 = AfentiLearningPlanUserRankStat.ck_uid_nbid(entity.getUserId(), entity.getNewBookId());
        getCache().getCacheObjectModifier().modify(key1, entityCacheExpirationInSeconds(),
                new ChangeCacheObject<List<AfentiLearningPlanUserRankStat>>() {
                    @Override
                    public List<AfentiLearningPlanUserRankStat> changeCacheObject(List<AfentiLearningPlanUserRankStat> currentValue) {
                        currentValue = new LinkedList<>(currentValue);
                        currentValue.add(entity);
                        return currentValue;
                    }
                });

        String key2 = AfentiLearningPlanUserRankStat.ck_uid_s(entity.getUserId(), entity.getSubject());
        getCache().getCacheObjectModifier().modify(key2, entityCacheExpirationInSeconds(),
                new ChangeCacheObject<Integer>() {
                    @Override
                    public Integer changeCacheObject(Integer currentValue) {
                        return currentValue + entity.getStar();
                    }
                });
        String key3 = AfentiLearningPlanUserRankStat.ck_iuid(entity.getUserId());
        getCache().getCacheObjectModifier().modify(key3, entityCacheExpirationInSeconds(),
                new ChangeCacheObject<Integer>() {
                    @Override
                    public Integer changeCacheObject(Integer currentValue) {
                        return currentValue + entity.getSilver() + entity.getSuccessiveSilver() + entity.getBonus();
                    }
                });
    }

    @UtopiaCacheable
    public List<AfentiLearningPlanUserRankStat> queryByUserIdAndNewBookId(@UtopiaCacheKey(name = "UID") Long userId,
                                                                          @UtopiaCacheKey(name = "NBID") String newBookId) {
        return dao.queryByUserIdAndNewBookId(userId, newBookId);
    }

    @UtopiaCacheable
    public int queryTotalStar(@UtopiaCacheKey(name = "UID") Long userId, @UtopiaCacheKey(name = "S") Subject subject) {
        BigDecimal value = dao.queryTotalStar(userId, subject);
        return value == null ? 0 : value.intValue();
    }

    @UtopiaCacheable
    public int queryTotalIntegarl(@UtopiaCacheKey(name = "UID_INTEGRAL") Long userId) {
        BigDecimal value = dao.queryTotalIntegarl(userId);
        return value == null ? 0 : value.intValue();
    }

    public boolean updateStat(Long id, Long userId, String newBookId, Subject subject, int star, int silver, int successiveSilver, int bonus) {
        int rows = dao.updateStat(userId, id, star, silver, successiveSilver, bonus);
        if (rows > 0) {
            String key1 = AfentiLearningPlanUserRankStat.ck_uid_nbid(userId, newBookId);
            getCache().getCacheObjectModifier().modify(key1, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<List<AfentiLearningPlanUserRankStat>>() {
                        @Override
                        public List<AfentiLearningPlanUserRankStat> changeCacheObject(List<AfentiLearningPlanUserRankStat> currentValue) {
                            currentValue = new LinkedList<>(currentValue);
                            for (AfentiLearningPlanUserRankStat stat : currentValue) {
                                if (!Objects.equals(id, stat.getId())) continue;
                                stat.setUpdateTime(new Date());
                                stat.setStar(stat.getStar() + star);
                                stat.setSilver(stat.getSilver() + silver);
                                stat.setSuccessiveSilver(stat.getSuccessiveSilver() + successiveSilver);
                                stat.setBonus(stat.getBonus() + bonus);
                            }
                            return currentValue;
                        }
                    });

            String key2 = AfentiLearningPlanUserRankStat.ck_uid_s(userId, subject);
            getCache().getCacheObjectModifier().modify(key2, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<Integer>() {
                        @Override
                        public Integer changeCacheObject(Integer currentValue) {
                            return currentValue + star;
                        }
                    });
            String key3 = AfentiLearningPlanUserRankStat.ck_iuid(userId);
            getCache().getCacheObjectModifier().modify(key3, entityCacheExpirationInSeconds(),
                    new ChangeCacheObject<Integer>() {
                        @Override
                        public Integer changeCacheObject(Integer currentValue) {
                            return currentValue + successiveSilver + silver + bonus;
                        }
                    });
        }
        return rows > 0;
    }
}
