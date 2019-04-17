package com.voxlearning.utopia.service.ai.cache.manager;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.ai.entity.TobbitMathOralBook;

import javax.inject.Named;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WIKI: http://wiki.17zuoye.net/pages/viewpage.action?pageId=45251113
 */
@Named
public class TobbitBoostCacheManager extends TobbitCacheManager {


    private static String TOBBIT_BOOST_TIME_BEGIN_CACHE = "m_p_t_b_t_begin"; // yyyy-MM-dd
    private static String TOBBIT_BOOST_TIME_END_CACHE = "m_p_t_b_t_end"; // yyyy-MM-dd
    private static String TOBBIT_BOOST_TIME_LIMIT_CACHE = "m_p_t_b_t_limit"; // seconds
    private static String TOBBIT_BOOST_PCOUNT_LIMIT_CACHE = "m_p_t_b_l_pcount"; // count/per

    private static String TOBBIT_BOOST_OLD_USER_ALLOW_CACHE = "m_p_t_b_o_u_allow"; // allow old user?


    private static String TOBBIT_USER_BOOST_NEW_CACHE = "m_p_t_u_boost_n_bid:%s";
    private static String TOBBIT_USER_BOOST_IN_PROGRESS_CACHE = "m_p_t_u_boost_i_p_openid:%s";

    private static String TOBBIT_BOOST_ORAL_BOOK_REMAIN_CACHE = "m_p_t_b_o_b_remain:%d";


    private static String TOBBIT_ALL_ORAL_BOOK_CACHE = "m_p_t_a_o_book";
    private static String TOBBIT_LIST_SCROLLING_CACHE = "m_p_t_l_scrolling";


    private static final int TOBBIT_ORAL_GRADE_MAX = 20000;
    private static final int TOBBIT_BOOST_TIME_LIMIT = 259200; // 72 hours
    private static final int TOBBIT_BOOST_PCOUNT_LIMIT = 5; // count per ticket
    private static final String TOBBIT_BOOST_TIME_END = "2019-04-30";
    private static final int[] grades = {1, 2, 3, 4};


    public Map<Integer, Integer> getBookRemain() {
        Map<Integer, Integer> map = new HashMap<>();
        for (int grade : grades) {
            map.put(grade, getBookRemain(grade));
        }
        return map;
    }


    public int getBookRemain(int grade) {
        String key = String.format(TOBBIT_BOOST_ORAL_BOOK_REMAIN_CACHE, grade);

        if (!exist(key)) {
            int count = TOBBIT_ORAL_GRADE_MAX;
            redisCommands.sync().getRedisStringCommands().set(key, SafeConverter.toString(count));
            return count;
        }

        Object obj = redisCommands.sync().getRedisStringCommands().get(key);
        return SafeConverter.toInt(obj);

    }

    public void decrBook(int grade) {

        String key = String.format(TOBBIT_BOOST_ORAL_BOOK_REMAIN_CACHE, grade);

        if (!exist(key)) {
            int count = TOBBIT_ORAL_GRADE_MAX - 1;
            redisCommands.sync().getRedisStringCommands().set(key, SafeConverter.toString(count));
        } else {
            redisCommands.sync().getRedisStringCommands().decrby(key, 1L);

        }

    }


    public boolean allowOldUser() {
        return exist(TOBBIT_BOOST_OLD_USER_ALLOW_CACHE);
    }

    public boolean isOpen() {
        LocalDate begin = toDate(get(TOBBIT_BOOST_TIME_BEGIN_CACHE));
        LocalDate end = toDate(get(TOBBIT_BOOST_TIME_END_CACHE));

        LocalDate now = LocalDate.now();
        if (begin != null) {
            if (begin.isAfter(now)) {
                return false;
            }
        }
        if (end == null) {
            end = LocalDate.parse(TOBBIT_BOOST_TIME_END);
        }

        return end.isAfter(now);
    }


    /**
     * @param bid bill id
     * @return -2: not exist, -1: persistence, >0: remain seconds
     */
    public long getBoostTimeRemain(String bid) {
        String key = String.format(TOBBIT_USER_BOOST_NEW_CACHE, bid);
        return ttl(key);

    }

    public boolean isInProgress(String openId) {
        String keyInprogress = String.format(TOBBIT_USER_BOOST_IN_PROGRESS_CACHE, openId);
        return exist(keyInprogress);
    }

    public void newBoostBill(String openId, String bid, String json) {
        String keyBid = String.format(TOBBIT_USER_BOOST_NEW_CACHE, bid);
        set(keyBid, json, boostTimeLimitSeconds());

        String keyOpenId = String.format(TOBBIT_USER_BOOST_IN_PROGRESS_CACHE, openId);
        set(keyOpenId, json, boostTimeLimitSeconds());

    }


    public void finishBoostBill(String bid, String openId) {
        String keyBid = String.format(TOBBIT_USER_BOOST_NEW_CACHE, bid);
        String keyOpenId = String.format(TOBBIT_USER_BOOST_IN_PROGRESS_CACHE, openId);
        expire(keyBid);
        expire(keyOpenId);
    }


    public List<Object> loadScrolling() {
        List<Object> list = redisCommands.sync().getRedisSetCommands().srandmember(TOBBIT_LIST_SCROLLING_CACHE, 10);
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }


    public void appendScrolling(String province, String name) {
        String value = province + StringUtils.nameObscure(name);
        if (value.length() > 3) {
            redisCommands.sync().getRedisSetCommands().sadd(TOBBIT_LIST_SCROLLING_CACHE, value);
        }
    }


    public List<TobbitMathOralBook> loadAllOralBook() {

        CacheObject<List<TobbitMathOralBook>> obj = CBS.get(TOBBIT_ALL_ORAL_BOOK_CACHE);
        if (obj.getValue() != null && obj.getValue().size() > 0) {
            return obj.getValue();
        } else {
            return Collections.emptyList();
        }
    }

    public void updateAllOralBook(List<TobbitMathOralBook> books) {

        if (books == null || books.isEmpty()) {
            CBS.delete(TOBBIT_ALL_ORAL_BOOK_CACHE);
        } else {
            CBS.set(TOBBIT_ALL_ORAL_BOOK_CACHE, TOBBIT_MATH_ALL_COURSE_EXPIRE, books);
        }

    }


    public int boostPCountLimit() {
        int count = SafeConverter.toInt(getObj(TOBBIT_BOOST_PCOUNT_LIMIT_CACHE));
        if (count > 0) {
            return count;
        }

        return TOBBIT_BOOST_PCOUNT_LIMIT;
    }


    public long boostTimeLimitSeconds() {
        long limit = SafeConverter.toLong(getObj(TOBBIT_BOOST_TIME_LIMIT_CACHE));
        if (limit > 0) {
            return limit;
        }
        return TOBBIT_BOOST_TIME_LIMIT;
    }

    public String boostEndDate() {
        String time = get(TOBBIT_BOOST_TIME_END_CACHE);
        if (nb(time)) {
            return time;
        }
        return TOBBIT_BOOST_TIME_END;
    }

}
