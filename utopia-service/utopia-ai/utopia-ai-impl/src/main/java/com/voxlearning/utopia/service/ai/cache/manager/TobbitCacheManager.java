package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.cache.redis.client.RedisClient;
import com.voxlearning.alps.cache.redis.client.RedisClientBuilder;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.ai.entity.TobbitMathCourse;

import javax.inject.Named;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

@Named
public class TobbitCacheManager extends SpringContainerSupport {


    public static final String TOBBIT_OCR_HISTORY_CACHE = "mini_program_tobbit_ocr_history:%s";

    public static final String TOBBIT_BOT_HISTORY_CACHE = "mini_program_tobbit_bot_history:%s";

    public static final String TOBBIT_USER_HISTORY_CACHE = "mini_program_tobbit_user_history:%s";

    private static String TOBBIT_USER_SCORE_DAY_CACHE = "mini_program_tobbit_user_score:%d_%s";

    private static String TOBBIT_USER_SCORE_CACHE = "m_p_t_u_score:%d";


    private static String TOBBIT_USER_INVITE_CACHE = "m_p_t_u_invite:%s_%s";


    private static String TOBBIT_MATH_ALL_COURSE_CACHE = "m_p_t_a_course";
    private static String TOBBIT_MATH_USER_COURSE_CACHE = "m_p_t_u_course:%d";


    private static String TOBBIT_MATH_USER_SP_CACHE = "m_p_t_u_sp:%s";


    public static final long TOBBIT_OCR_HISTORY_EXPIRE = 5 * 60;

    public static final long TOBBIT_BOT_HISTORY_EXPIRE = 60;

    public static final long TOBBIT_USER_HISTORY_EXPIRE = 10;


    public static final int TOBBIT_USER_INVITE_EXPIRE = 7776000; // 3 month

    public static final int TOBBIT_MONTH_EXPIRE = 2592000; // 1 month

    public static final int TOBBIT_MATH_ALL_COURSE_EXPIRE = 5 * 60;

    public static final int TOBBIT_USER_SP_EXPIRE = 3600 * 24 * 7; // 1 week


    protected UtopiaCache CBS;


    protected IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisClientBuilder clientBuilder = RedisClientBuilder.Companion.getInstance();
        RedisClient client = clientBuilder.getRedisClient("mp-redis");
        if (client != null) {
            redisCommands = client.getRedisCommands();
        }

        // CBS
        CBS = CacheSystem.CBS.getCacheBuilder().getCache("persistence");
    }


    public void markSp(String openId, String sp) {
        String key = String.format(TOBBIT_MATH_USER_SP_CACHE, openId);
        redisCommands.sync().getRedisHashCommands().hset(key, "sp", sp);
        redisCommands.sync().getRedisKeyCommands().expire(key, TOBBIT_USER_SP_EXPIRE);
    }

    public void markSpEffect(String openId) {
        String key = String.format(TOBBIT_MATH_USER_SP_CACHE, openId);
        if (existSP(openId)) {
            redisCommands.sync().getRedisHashCommands().hset(key, "e", 1);
        }
    }

    public String getEffectSp(String openId) {
        String key = String.format(TOBBIT_MATH_USER_SP_CACHE, openId);
        Object obj = redisCommands.sync().getRedisHashCommands().hget(key, "e");
        if (obj != null) {
            return getSP(openId);
        }
        return "";
    }

    public String getSP(String openId) {
        String key = String.format(TOBBIT_MATH_USER_SP_CACHE, openId);
        Object obj = redisCommands.sync().getRedisHashCommands().hget(key, "sp");
        if (obj != null) {
            return SafeConverter.toString(obj);
        }
        return "";
    }

    public boolean existSP(String openId) {
        String key = String.format(TOBBIT_MATH_USER_SP_CACHE, openId);
        return exist(key);
    }


    public List<TobbitMathCourse> loadAllCourse() {
        CacheObject<List<TobbitMathCourse>> obj = CBS.get(TOBBIT_MATH_ALL_COURSE_CACHE);
        if (obj.getValue() != null && obj.getValue().size() > 0) {
            return obj.getValue();
        } else {
            return Collections.emptyList();
        }
    }

    public void updateAllCourse(List<TobbitMathCourse> courses) {
        if (courses == null || courses.isEmpty()) {
            CBS.delete(TOBBIT_MATH_ALL_COURSE_CACHE);
        } else {
            CBS.set(TOBBIT_MATH_ALL_COURSE_CACHE, TOBBIT_MATH_ALL_COURSE_EXPIRE, courses);
        }

    }

    public List<TobbitMathCourse> loadUserCourse(Long uid) {
        String key = String.format(TOBBIT_MATH_USER_COURSE_CACHE, uid);
        CacheObject<List<TobbitMathCourse>> obj = CBS.get(key);
        if (obj.getValue() != null && obj.getValue().size() > 0) {
            return obj.getValue();
        } else {
            return Collections.emptyList();
        }
    }

    public void updateUserCourse(Long uid, List<TobbitMathCourse> courses) {

        String key = String.format(TOBBIT_MATH_USER_COURSE_CACHE, uid);
        if (courses == null) {
            CBS.delete(key);
        } else {
            CBS.set(key, TOBBIT_MATH_ALL_COURSE_EXPIRE - 10, courses);
        }
    }


    public boolean invited(String openId, String inviter) {
        String key = String.format(TOBBIT_USER_INVITE_CACHE, openId, inviter);

        Object obj = redisCommands.sync().getRedisStringCommands().get(key);
        if (obj != null) {
            return true;
        }
        // set
        set(key, "1", TOBBIT_USER_INVITE_EXPIRE);
        return false;
    }


    public long getTotalScore(Long uid) {
        String key = String.format(TOBBIT_USER_SCORE_CACHE, uid);
        return getScore(key);
    }


    public void setTotalScore(Long uid, long score) {
        String key = String.format(TOBBIT_USER_SCORE_CACHE, uid);
        redisCommands.sync().getRedisStringCommands().set(key, score);
    }


    public long minusTotalScore(Long uid, long score) {
        String key = String.format(TOBBIT_USER_SCORE_CACHE, uid);
        return minusScore(key, score);
    }


    public long getTodayScore(Long uid) {
        String key = String.format(TOBBIT_USER_SCORE_DAY_CACHE, uid, today());
        return getScore(key);
    }


    public long addTodayScore(Long uid, long score) {
        String key = String.format(TOBBIT_USER_SCORE_DAY_CACHE, uid, today());
        return addScore(key, score);
    }


    public long getScore(String key) {
        Object obj = redisCommands.sync().getRedisStringCommands().get(key);
        if (obj != null) {
            return Long.parseLong(String.valueOf(obj));
        }
        return 0;
    }


    public long addScore(String key, long score) {
        long ret = redisCommands.sync().getRedisStringCommands().incrby(key, score);
        return ret;
    }


    public long minusScore(String key, long score) {
        long ret = redisCommands.sync().getRedisStringCommands().incrby(key, score);
        return ret;
    }


    public boolean existOcrCache(String md5) {
        String key = String.format(TOBBIT_OCR_HISTORY_CACHE, md5);
        return exist(key);
    }

    public Long expireOcrCache(String md5) {
        String key = String.format(TOBBIT_OCR_HISTORY_CACHE, md5);
        return expire(key);
    }


    public String getOcrCache(String md5) {
        String key = String.format(TOBBIT_OCR_HISTORY_CACHE, md5);
        return get(key);
    }

    public void setOcrCache(String md5, String json) {
        String key = String.format(TOBBIT_OCR_HISTORY_CACHE, md5);
        set(key, json, TOBBIT_OCR_HISTORY_EXPIRE);
    }


    public boolean existBotCache(String md5) {
        String key = String.format(TOBBIT_BOT_HISTORY_CACHE, md5);
        return exist(key);
    }


    public String getBotCache(String md5) {
        String key = String.format(TOBBIT_BOT_HISTORY_CACHE, md5);
        return get(key);
    }

    public void setBotCache(String md5, String json) {
        String key = String.format(TOBBIT_BOT_HISTORY_CACHE, md5);
        set(key, json, TOBBIT_BOT_HISTORY_EXPIRE);
    }


    public boolean existUserHistoryCache(String id) {
        String key = String.format(TOBBIT_USER_HISTORY_CACHE, id);
        return exist(key);
    }


    public String getUserHistoryCache(String id) {
        String key = String.format(TOBBIT_USER_HISTORY_CACHE, id);
        return get(key);
    }

    public void setUserHistoryCache(String id, String json) {
        String key = String.format(TOBBIT_USER_HISTORY_CACHE, id);
        set(key, json, TOBBIT_USER_HISTORY_EXPIRE);
    }


    public boolean exist(String key) {
        return ttl(key) > -2;
    }

    public long ttl(String key) {
        return redisCommands.sync().getRedisKeyCommands().ttl(key);
    }


    public String get(String key) {
        Object obj = redisCommands.sync().getRedisStringCommands().get(key);
        return SafeConverter.toString(obj);
    }

    public Object getObj(String key) {
        return redisCommands.sync().getRedisStringCommands().get(key);
    }

    public void set(String key, String json, long seconds) {
        redisCommands.sync().getRedisStringCommands().setex(key, seconds, json);
    }

    public Long expire(String key) {
        return redisCommands.sync().getRedisKeyCommands().del(key);
    }


    protected String today() {
        return DateUtils.getTodaySqlDate();
    }

    protected boolean nb(CharSequence cs) {
        return StringUtils.isNotBlank(cs);
    }


    protected LocalDate toDate(String dateSrc) {
        if (!nb(dateSrc)) {
            return null;
        }
        try {
            return LocalDate.parse(dateSrc);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
