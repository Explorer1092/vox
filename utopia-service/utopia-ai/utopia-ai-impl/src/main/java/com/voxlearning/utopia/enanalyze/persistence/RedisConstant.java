package com.voxlearning.utopia.enanalyze.persistence;

import lombok.AllArgsConstructor;

/**
 * redis缓存相关常量
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface RedisConstant {

    /**
     * 源
     */
    String CONFIG = "chips-redis";

    /**
     * redis建前缀
     */
    String KEY_PREFIX = "en_analyze";

    @AllArgsConstructor
    enum Key {

        IDEMPOTENT("idempotent", "幂等", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        LIKE_SENTENCE_COUNTER("like:sentence:counter", "句子点赞数", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        LIKE_SENTENCE_SET("like:sentence:set", "句子点赞集合", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        LIKE_SENTENCE_STATUS("like:sentence:status", "句子点赞状态", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },

        USER_SESSION("user:session", "用户会话", 30 * 24 * 60 * 60L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        SENTENCE("sentence", "句子", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        RANK_SENTENCE("rank:sentence", "句子排行榜", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        RANK_ABILITY("rank:ability", "能力排行榜", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        },
        RANK_FREQUENCY("rank:frequency", "能力排行榜", 0L) {
            @Override
            public String getKey() {
                return KEY_PREFIX + ":" + KEY;
            }
        };
        final String KEY;
        public final String DESC;
        public final long TIMEOUT;

        /**
         * 获取键
         *
         * @return
         */
        public abstract String getKey();
    }
}
