package com.voxlearning.utopia.service.wechat.cache;

import com.voxlearning.alps.cache.redis.client.RedisClient;
import com.voxlearning.alps.cache.redis.client.RedisClientBuilder;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.wechat.api.constants.MiniProgramType;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.*;


@Named
@Slf4j
public class UserMiniProgramCacheManager extends SpringContainerSupport {


    // MiniProgramType_%s
    private static String DAY_CHECK_PREFIX = "MINI_PROGRAM_CHECK_DAY:%s_%s";
    private static String USER_CHECK_PREFIX = "MINI_PROGRAM_CHECK_USER:%s_%d";

    private static String USER_PUSH_FORMIDS = "MINI_PROGRAM_PUSH_FORMIDS_USER:%s_%d";



    private static final String IS_DOWN_DAY_PLAY_PUSH_KEY = "MINI_PROGRAM_DAY_PLAN_IS_DOWN_PUSH";



    // Distribution lock
//    private static final AtomicLockManager lock = AtomicLockManager.getInstance();



    private static final long THREE_MONTH_SECONDS = 7776000; // 3/month
    private static final long DAY_CHECK_EXPIRED_SECONDS = THREE_MONTH_SECONDS;


    private static final long TODAY_COUNT = 2872; // min count
    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisClientBuilder clientBuilder = RedisClientBuilder.Companion.getInstance();
        RedisClient client = clientBuilder.getRedisClient("mp-redis");
        if (client != null) {
            redisCommands = client.getRedisCommands();
        }
    }


    public IRedisCommands getRedisCommands() {
        return redisCommands;
    }

    public long todayCheckedCount(MiniProgramType type) {
        String key = String.format(DAY_CHECK_PREFIX, type,today());

        Long count = redisCommands.sync().getRedisSetCommands().scard(key);
        if (count != null) {
            return TODAY_COUNT+count;
        }
        return TODAY_COUNT;
    }


    public void checking(Long uid,MiniProgramType type) {
        // Today count
        // Because our system uid is to long,use BitSet will expend a lot of memory, give up,using Set instead of.
        String key = String.format(DAY_CHECK_PREFIX,type, today());
        redisCommands.sync().getRedisSetCommands().sadd(key, uid);
        redisCommands.sync().getRedisKeyCommands().expire(key, DAY_CHECK_EXPIRED_SECONDS);
    }


    /**
     * @param uid
     * @return map {checking:222,checked:22222}
     */
    public Map<String, Object> getUserCheckData(Long uid,MiniProgramType type) {
        String key = String.format(USER_CHECK_PREFIX, type,uid);
        return redisCommands.sync().getRedisHashCommands().hgetall(key);
    }


    public void setUserCheckData(Long uid, Integer checking, Integer checked,MiniProgramType type) {
        String key = String.format(USER_CHECK_PREFIX, type,uid);
        Map<String, Object> map = new HashMap<>();
        map.put("checking", checking);
        map.put("checked", checked);
        redisCommands.async().getRedisHashAsyncCommands().hmset(key, map);
    }


    public boolean isChecked(Long uid, MiniProgramType type) {

        // Today count
        String key = String.format(DAY_CHECK_PREFIX, type,today());
        return redisCommands.sync().getRedisSetCommands().sismember(key, uid);
    }


    public int getWeekContinuousCheckCount(Long uid,MiniProgramType type) {

        Map<String, Object> weekData = currentWeekDayInfo();
        List weekDays = (List) weekData.get("weekDays");
        int diff = SafeConverter.toInt(weekData.get("diff"));

        int checkingCount = 0;
        for (int i = 0; i < weekDays.size(); i++) {
            String dayName = String.valueOf(weekDays.get(i));
            // Avoid loop some useless request to redis
            if (i <= diff) {
                // Read from redis
                String key = String.format(DAY_CHECK_PREFIX, type,dayName);
                Boolean bit=redisCommands.sync().getRedisSetCommands().sismember(key, uid);
                // Continuous check?
                if (bit) {
                    checkingCount++;
                }else if(today().equals(dayName)){
                    // Today not done check
                    // Keep checking count, not reset
                } else {
                    // Reset
                    checkingCount = 0;
                }
            } else {
                // Fill data,use default value
            }

        }
        return checkingCount;
    }


    public void addUserPushFormIds(Long pid, String formId,MiniProgramType type) {
        String key = String.format(USER_PUSH_FORMIDS,type, pid);
        redisCommands.sync().getRedisStringCommands().set(key, formId);
    }

    public String getUserPushFormId(Long pid,MiniProgramType type) {
        String key = String.format(USER_PUSH_FORMIDS, type,pid);
        Object id = redisCommands.sync().getRedisStringCommands().get(key);
        if (id != null) {
            return String.valueOf(id);
        }
        return "";
    }

    private Map<String, Object> currentWeekDayInfo() {

        Map<String, Object> map = new HashMap<>();
        WeekRange range = WeekRange.current();
        Date start = range.getStartDate();

        Date today = new Date();

        long diff = DateUtils.dayDiff(today, start);
        if (diff < 0) {
            diff = 0;
        }

        List<String> weekDays = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDays.add(DateUtils.dateToString(DateUtils.addDays(start, i), DateUtils.FORMAT_SQL_DATE));
        }

        map.put("diff", diff);
        map.put("weekDays", weekDays);
        return map;
    }


    private String today() {
        return DateUtils.getTodaySqlDate();
    }


}
