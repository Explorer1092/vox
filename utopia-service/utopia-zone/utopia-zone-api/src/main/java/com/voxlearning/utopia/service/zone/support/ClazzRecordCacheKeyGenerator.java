package com.voxlearning.utopia.service.zone.support;

import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;

/**
 * Created by Yuechen.Wang on 2017/4/27.
 */
@UtopiaCacheRevision("20171206")
public class ClazzRecordCacheKeyGenerator {

    // 学霸之星卡片
    public static String ck_sm_card(String homeworkId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "sm", homeworkId);
    }

    // 学霸之星 TOP3
    public static String ck_sm_top3(String homeworkId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "sm_top3", homeworkId);
    }

    // 专注之星卡片
    public static String ck_focus_card(String homeworkId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "focus", homeworkId);
    }

    // 专注之星 TOP3
    public static String ck_focus_top3(String homeworkId) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "focus_top3", homeworkId);
    }

    // 明察之星卡片
    public static String ck_sharp_card(String sharpKey) {
        // sort the classmates and use the hash value as part of the sharp key
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "sharp", sharpKey);
    }

    // 明察之星 TOP3 以及 本周最佳
    public static String ck_sharp_map(String sharpKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "sharp_map", sharpKey);
    }

    // 装扮之星卡片
    public static String ck_fashion_card(String fashionKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "fashion", fashionKey);
    }

    // 装扮之星 TOP3
    public static String ck_fashion_top3(String fashionKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "fashion_top3", fashionKey);
    }

    // 装扮之星 本周最佳
    public static String ck_fashion_week(String fashionKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "fashion_week", fashionKey);
    }

    // 友爱之星卡片
    public static String ck_friendship_card(String friendship) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "friendship", friendship);
    }

    // 友爱之星 TOP3
    public static String ck_friendship_top3(String friendship) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "friendship_top3", friendship);
    }

    // 友爱之星 本周最佳
    public static String ck_friendship_week(String friendship) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "friendship_week", friendship);
    }

    // 满分之星卡片
    public static String ck_fullmarks_card(String fullmarks) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "fullmarks", fullmarks);
    }

    // 满分之星 TOP3
    public static String ck_fullmarks_top3(String fullmarks) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "fullmarks_top3", fullmarks);
    }

    // 满分之星 本周最佳
    public static String ck_fullmarks_week(String fullmarks) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "fullmarks_week", fullmarks);
    }

    // 毅力之星卡片
    public static String ck_stamina_card(String staminaKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "stamina_card", staminaKey);
    }

    // 毅力之星 TOP3
    public static String ck_stamina_top3(String staminaKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "stamina_top3", staminaKey);
    }

    // 毅力之星 上周最佳
    public static String ck_stamina_week(String staminaKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "stamina_week", staminaKey);
    }

    // 闯关之星卡片
    public static String ck_cracker_card(String crackerKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "cracker_card", crackerKey);
    }

    // 闯关之星 TOP3
    public static String ck_cracker_top3(String crackerKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "cracker_top3", crackerKey);
    }

    // 闯关之星 上周最佳
    public static String ck_cracker_week(String crackerKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "cracker_week", crackerKey);
    }

    // 竞技之星卡片
    public static String ck_competition_card(String competitionKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "competition_card", competitionKey);
    }

    // 竞技之星 TOP3
    public static String ck_competition_top3(String competitionKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "competition_top3", competitionKey);
    }

    // 竞技之星 上周最佳
    public static String ck_competition_week(String competitionKey) {
        return CacheKeyGenerator.generateCacheKey(ClazzRecordCardMapper.class, "competition_week", competitionKey);
    }

}
