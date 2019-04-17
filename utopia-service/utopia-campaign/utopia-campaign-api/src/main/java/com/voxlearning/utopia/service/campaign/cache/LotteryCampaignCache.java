//package com.voxlearning.utopia.service.campaign.cache;
//
//import com.voxlearning.alps.annotation.cache.CacheSystem;
//import com.voxlearning.alps.core.util.CacheKeyGenerator;
//import com.voxlearning.alps.core.util.StringUtils;
//import com.voxlearning.alps.spi.cache.UtopiaCache;
//
//import java.util.Date;
//
///**
// * Lottery Campaign Cache
// * Created by alex on 2018/3/20.
// */
//public class LotteryCampaignCache {
//    public static UtopiaCache getCache() {
//        return lotteryCacheHolder.adCache;
//    }
//
//    private static class lotteryCacheHolder {
//        private static final UtopiaCache adCache;
//
//        static {
//            adCache = CacheSystem.CBS.getCache("storage");
//        }
//    }
//
//    public static void addBigRewardRecord(String campaignId, Long userId, Long schoolId, Integer regionCode, Date expireDate) {
//
//        // 中奖个人记录
//        String cacheKeyUser = ckBigRewardUser(campaignId, userId);
//        getCache().set(cacheKeyUser, (int) (expireDate.getTime() / 1000), "1");
//
//        // 中奖个人记录
//        String cacheKeySchool = ckBigRewardSchool(campaignId, schoolId);
//        getCache().set(cacheKeySchool, (int) (expireDate.getTime() / 1000), "1");
//
//        // 中奖个人地区
//        String cacheKeyRegion = ckBigRewardRegion(campaignId, regionCode);
//        getCache().set(cacheKeyRegion, (int) (expireDate.getTime() / 1000), "1");
//    }
//
//    public static boolean userWonBigAward(String campaignId, Long userId) {
//        String cacheKeyUser = ckBigRewardUser(campaignId, userId);
//        String value = getCache().load(cacheKeyUser);
//        return StringUtils.isNoneBlank(value);
//    }
//
//    public static boolean schoolWonBigAward(String campaignId, Long schoolId) {
//        String cacheKeySchool = ckBigRewardSchool(campaignId, schoolId);
//        String value = getCache().load(cacheKeySchool);
//        return StringUtils.isNoneBlank(value);
//    }
//
//    public static boolean regionWonBigAward(String campaignId, Integer regionCode) {
//        String cacheKeyRegion = ckBigRewardRegion(campaignId, regionCode);
//        String value = getCache().load(cacheKeyRegion);
//        return StringUtils.isNoneBlank(value);
//    }
//
//    private static String ckBigRewardUser(String campaignId, Long userId) {
//        return CacheKeyGenerator.generateCacheKey(LotteryCampaignCache.class, new String[]{"C", "U"}, new Object[]{campaignId, userId});
//    }
//
//    private static String ckBigRewardSchool(String campaignId, Long schoolId) {
//        return CacheKeyGenerator.generateCacheKey(LotteryCampaignCache.class, new String[]{"C", "S"}, new Object[]{campaignId, schoolId});
//    }
//
//    private static String ckBigRewardRegion(String campaignId, Integer regionCode) {
//        return CacheKeyGenerator.generateCacheKey(LotteryCampaignCache.class, new String[]{"C", "R"}, new Object[]{campaignId, regionCode});
//    }
//
//}
