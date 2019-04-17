package com.voxlearning.utopia.service.reward.util;


public class CacheKeyUtils {

    /**
     * 班级缓存 , hash 结构, filed 存教室id ,value 教室信息
     *
     * @param activityId
     * @param clazz
     * @return
     */
    public static String genClassCollectKey(Long activityId, Long clazz) {
        return String.format("public_good:%s:clazz:%s", activityId, clazz);
    }

    /**
     * 第三方点赞数量统计 ,string 结构,  value 存 数量
     *
     * @param activityId
     * @param userId
     * @param source
     * @return
     */
    public static String genLikeKey(Long activityId, Long userId, String source) {
        return String.format("public_good:%s:like:%s:%s", activityId, userId, source);
    }

    /**
     * 用户点赞过的教室 , hash 结构, filed 存用户ID value 为空
     *
     * @param activityId
     * @param userId
     * @return
     */
    public static String genLikedKey(Long activityId, Long userId) {
        return String.format("public_good:%s:liked:%s", activityId, userId);
    }

    /**
     * 用户是否有新动态 , string 结构 , bigmap
     *
     * @param activityId
     * @return
     */
    public static String genFeedKey(Long activityId) {
        return String.format("public_good:%s:feed", activityId);
    }

    public static void main(String[] args) {
        System.out.println(genClassCollectKey(123L, 111L));
        //System.out.println(genLikeKey(123L));
        System.out.println(genLikedKey(123L, 123L));
        System.out.println(genFeedKey(123L));

        String cacheKey = CacheKeyUtils.genLikeKey(123L, 30016L, "PC");
        System.out.println(cacheKey);
    }
}
