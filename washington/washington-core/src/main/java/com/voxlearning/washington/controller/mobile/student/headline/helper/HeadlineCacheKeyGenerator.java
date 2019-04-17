package com.voxlearning.washington.controller.mobile.student.headline.helper;

/**
 * 用于管理头条相关的缓存Key
 *
 * @author yuechen.wang
 * @since 2017/11/01
 */

public class HeadlineCacheKeyGenerator {

    public static int CACHE_ONE_WEEK = 7 * 24 * 60 * 60;

    /**
     * 班级新鲜事列表key
     */
    static String clazzHeadlineKey(Long clazzId) {
        return "APPLICATION_STD_CLAZZ_JOURNAL_" + clazzId;
    }

    /**
     * 获取不需要弹框分享成就key  两种 1 关闭分享的成就 2 已经分享的成就
     */
    static String achievementHiddenKey(Long userId) {
        return "STUDENT_APP_HEADLINE_STUDENT_ACHIEVEMENT_HIDDEN_" + userId;
    }

    /**
     * 个人分享鼓励列表 key
     * FIXME 此项暂时不要随意改动，涉及了业务数据，后续就下了
     */
    public static String achievementEncouragerKey(Object id) {
        return "STUDENT_APP_HEADLINE_STUDENT_ACHIEVEMENT_ENCOURAGE_" + id;
    }

    /**
     * 生日祝福列表 key
     */
    public static String birthdayBlessKey(String id) {
        return "STUDENT_APP_HEADLINE_STUDENT_BIRTHDAY_BLESS_" + id;
    }

    /**
     * 学生数量缓存 key
     */
    public static String studentCountKey(Long userId) {
        return "STUDENT_APP_HEADLINE_STUDENT_COUNT_" + userId;
    }

}
