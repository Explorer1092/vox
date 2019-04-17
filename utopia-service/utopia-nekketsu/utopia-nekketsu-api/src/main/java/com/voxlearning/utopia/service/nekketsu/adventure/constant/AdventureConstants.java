package com.voxlearning.utopia.service.nekketsu.adventure.constant;

/**
 * 常量
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/8/31 17:58
 */
public class AdventureConstants {

    public static final Integer STAGE_WORDS_COUNT = 7;//每关单词数

    public static final Integer DEFAULT_OPEN_STAGE = 15;//默认开启关卡数

    public static final Integer SPOKEN_APP_COUNT = 1;//口语应用个数

    public static final Integer MAX_STAGE = 60;//最大关卡数

    public static final Integer OPEN_15_STAGE_CROWN = 9;//最大关卡数

    public static final Integer RECEIVE_FREE_MAX_STAGE = 5;//免费领取学习豆最大试用关卡

    public static final Integer STAGEAPP_MAX_DIAMOND = 3;

    public static final Integer TRIAL_COUNT = 5;

    public static final String NEW_GIFT_CACHE_KEY_PREFIX = "ADVENTURE_NEW_GIFT:";

    public static final String NEW_ACHIEVEMENT_CACHE_KEY_PREFIX = "ADVENTURE_NEW_ACHIEVEMENT:";

    public static final String NEW_STAGE_CACHE_KEY_PREFIX = "ADVENTURE_NEW_STAGE:";

    public static final String LOGIN_CACHE_KEY_PREFIX = "ADVENTURE_LOGIN:";

    public static final String USER_PAID_KEY_PREFIX = "ADVENTURE_USER_PAID:";

    public static class BaseAppCount {
        private static final Integer BASE_APP_COUNT_MAX = 4; //基础应用个数

        public static Integer BASE_APP_COUNT(Integer classLevel) {
            if (1 <= classLevel &&  3 >= classLevel) {
                return BASE_APP_COUNT_MAX - 1;
            }
            return BASE_APP_COUNT_MAX;
        }
    }

}
