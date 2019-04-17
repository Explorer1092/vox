package com.voxlearning.utopia.service.newhomework.impl.dao.outside;

import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingAchievement;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/15
 */
@Named
@CacheBean(type = OutsideReadingAchievement.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class OutsideReadingAchievementDao extends StaticMongoShardPersistence<OutsideReadingAchievement, Long> {

    @Override
    protected void calculateCacheDimensions(OutsideReadingAchievement document, Collection<String> dimensions) {
        dimensions.add(OutsideReadingAchievement.ck_id(document.getId()));
    }

    /**
     * 增加成就字数
     * @param userId 学生ID
     * @param clazzLevel 年级
     * @param addReadingCount 要增加的字数
     */
    public void addReadingCount(Long userId, Integer clazzLevel, Double addReadingCount) {
        if (addReadingCount <= 0 || clazzLevel > 6 || clazzLevel < 1) {
            return;
        }
        OutsideReadingAchievement readingAchievement = load(userId);
        OutsideReadingAchievement achievement = new OutsideReadingAchievement();
        Date currentDate = new Date();
        List<Double> levelReadingCount = null;
        Double totalClazzLevelReadingCount = addReadingCount;
        if (readingAchievement != null) {
            Double oldClazzLevelReadingCount = readingAchievement.getLevelReadingCount().get(clazzLevel - 1);
            totalClazzLevelReadingCount = oldClazzLevelReadingCount + addReadingCount;
            levelReadingCount = readingAchievement.getLevelReadingCount();
        } else {
            achievement.setGoldenWordsCount(0);
            achievement.setCreateAt(currentDate);
            levelReadingCount = Lists.newArrayList(0D, 0D, 0D, 0D, 0D, 0D);
        }
        achievement.setId(userId);
        achievement.setUpdateAt(currentDate);
        levelReadingCount.set(clazzLevel - 1, totalClazzLevelReadingCount);
        achievement.setLevelReadingCount(levelReadingCount);
        upsert(achievement);
    }

    /**
     * 增加好词好句数量
     * @param userId 学生ID
     * @param addGoldenWordsCount 要增加的好词好句数
     */
    public void addGoldenWordsCount(Long userId, Integer addGoldenWordsCount) {
        OutsideReadingAchievement readingAchievement = load(userId);
        OutsideReadingAchievement achievement = new OutsideReadingAchievement();
        Date currentDate = new Date();
        if (readingAchievement != null) {
            addGoldenWordsCount += readingAchievement.getGoldenWordsCount();
        } else {
            achievement.setLevelReadingCount(Lists.newArrayList(0D, 0D, 0D, 0D, 0D, 0D));
            achievement.setCreateAt(currentDate);
        }
        achievement.setId(userId);
        achievement.setUpdateAt(currentDate);
        achievement.setGoldenWordsCount(addGoldenWordsCount);
        upsert(achievement);
    }
}
