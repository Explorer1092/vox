package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author RA
 */
@Named
@Slf4j
public class  MiniProgramCacheManager extends SpringContainerSupport {


    private static String DAY_CHECK_PREFIX = "MINI_PROGRAM_CHECK_DAY:%s";
    private static String USER_CHECK_PREFIX = "MINI_PROGRAM_CHECK_USER:%d";

    private static String USER_DAY_PLAN_PREFIX = "MINI_PROGRAM_DAY_PLAN_USER:%d_%d";
    private static String USER_READ_TIME_PREFIX = "MINI_PROGRAM_READ_TIME_USER:%s:%d";
    private static String USER_PUSH_FORMIDS = "MINI_PROGRAM_PUSH_FORMIDS_USER:%d";


    private static final String DAY_PLAN_UPDATE_LOCK_KEY = "MINI_PROGRAM_DAY_PLAN_UPDATE_LOCK:%d";


    private static final String IS_DOWN_DAY_PLAY_PUSH_KEY = "MINI_PROGRAM_DAY_PLAN_IS_DOWN_PUSH";



    // Distribution lock
    private static final AtomicLockManager lock = AtomicLockManager.getInstance();



    private static final long THREE_MONTH_SECONDS = 7776000; // 3/month
    private static final long DAY_CHECK_EXPIRED_SECONDS = THREE_MONTH_SECONDS;
    private static final long DAY_READ_EXPIRED_SECONDS = 1209600; // 2/week


    private static final long TODAY_COUNT = 2872; // min count
    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("piclisten-redis");
    }


    public IRedisCommands getRedisCommands() {
        return redisCommands;
    }

    public long todayCheckedCount() {
        String key = String.format(DAY_CHECK_PREFIX, today());

        Long count = redisCommands.sync().getRedisSetCommands().scard(key);
        if (count != null) {
            return TODAY_COUNT+count;
        }
        return TODAY_COUNT;
    }


    public void checking(Long uid) {
        // Today count
        // Because our system uid is to long,use BitSet will expend a lot of memory, give up,using Set instead of.
        String key = String.format(DAY_CHECK_PREFIX, today());
        redisCommands.sync().getRedisSetCommands().sadd(key, uid);
        redisCommands.sync().getRedisKeyCommands().expire(key, DAY_CHECK_EXPIRED_SECONDS);
    }


    /**
     * @param uid
     * @return map {checking:222,checked:22222}
     */
    public Map<String, Object> getUserCheckData(Long uid) {
        String key = String.format(USER_CHECK_PREFIX, uid);
        return redisCommands.sync().getRedisHashCommands().hgetall(key);
    }


    public void setUserCheckData(Long uid, Integer checking, Integer checked) {
        String key = String.format(USER_CHECK_PREFIX, uid);
        Map<String, Object> map = new HashMap<>();
        map.put("checking", checking);
        map.put("checked", checked);
        redisCommands.async().getRedisHashAsyncCommands().hmset(key, map);
    }


    public boolean isChecked(Long uid) {
        // Today count
        String key = String.format(DAY_CHECK_PREFIX, today());
        return redisCommands.sync().getRedisSetCommands().sismember(key, uid);
    }




    public void setUserDayPlanData(Long pid, Long uid,Integer planMinutes, int remind, String remindTime) {
        String lockKey = String.format(DAY_PLAN_UPDATE_LOCK_KEY, pid);

        try {

            lock.acquireLock(lockKey);

            String key = String.format(USER_DAY_PLAN_PREFIX, pid,uid);
            Map<String, Object> map = new HashedMap<>();
            map.put("plan_minutes", planMinutes);
            map.put("is_remind", remind);
            map.put("remind_time", remindTime);
            redisCommands.sync().getRedisHashCommands().hmset(key, map);
            // Not expire
            redisCommands.sync().getRedisKeyCommands().expire(key, Integer.MAX_VALUE);


            // !! 推送
            key = String.format("MINI_PROGRAM_DAY_PLAN_PUSH_KEYS:%d_%d", pid,uid);

            // Get keys
            Set<Object> keys = redisCommands.sync().getRedisSetCommands().smembers(key);
            if (keys != null && keys.size() > 0) {
                // Remove old jobs data
                keys.forEach(node -> {
                    String value = pid + "_" + uid;
                    redisCommands.sync().getRedisSetCommands().srem(String.valueOf(node), value);
                });

            }

            // New data
            Set<String> newKeys = new HashSet<>(7);
            // Set new jobs,1 week
            for (int i = 1; i < 8; i++) {
                String newKey = getWeekDayTimeKey(i, remindTime);
                // add push jobs
                String value=pid + "_" + uid;
                redisCommands.sync().getRedisSetCommands().sadd(newKey, value);
                newKeys.add(newKey);
            }

            // Set new keys
            redisCommands.sync().getRedisKeyCommands().del(key);
            redisCommands.sync().getRedisSetCommands().sadd(key, newKeys);


        } catch (CannotAcquireLockException e) {
            // Cant't get lock
            log.debug("The key [{}] can't get lock on incrReadData method", lockKey);

        } finally {
            lock.releaseLock(lockKey);
        }

    }


    public Set<String> getDayPlanPushTask() {

        Set<Object> pids = redisCommands.sync().getRedisSetCommands().smembers(genWeekDayTimeKey());
        if (pids != null && !pids.isEmpty()) {
            return pids.stream().map(SafeConverter::toString).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }


    public boolean isDoneDayPlanPush() {
        long offset=Instant.now().getEpochSecond()/60;
        long bit = redisCommands.sync().getRedisStringCommands().getbit(IS_DOWN_DAY_PLAY_PUSH_KEY, offset);
        return 1==bit;
    }

    public void doneDayPlanPush() {
        long offset=Instant.now().getEpochSecond()/60;
        redisCommands.sync().getRedisStringCommands().setbit(IS_DOWN_DAY_PLAY_PUSH_KEY, offset, 1);
    }



    public Map<String, Object> getUserDayPlanData(Long pid,Long uid) {
        String key = String.format(USER_DAY_PLAN_PREFIX, pid,uid);
        Map<String, Object> rmap=redisCommands.sync().getRedisHashCommands().hgetall(key);

        Integer planMinutes = SafeConverter.toInt(rmap.get("plan_minutes"));
        if (planMinutes <= 0) {
            rmap.put("plan_minutes", 10);
        }
        return rmap;

    }



    public boolean isRemindUserDayPlan(Long pid,Long uid) {
        Map<String, Object> obj = getUserDayPlanData(pid,uid);
        if (obj != null) {
            int remind = SafeConverter.toInt(obj.get("is_remind"));
            if (remind > 0) {
                return true;
            }
        }
        return false;

    }
    public String getUserDayPlanRemindTime(Long pid,Long uid) {
        Map<String, Object> obj = getUserDayPlanData(pid,uid);
        if (obj != null) {
            return String.valueOf(obj.get("remind_time"));
        }
        return "";

    }


    public int getTodayReadPlan(Long pid,Long uid) {
        String key = String.format(USER_DAY_PLAN_PREFIX, pid,uid);
        Object obj = redisCommands.sync().getRedisHashCommands().hget(key, "plan_minutes");
        int time = SafeConverter.toInt(obj);
        time=time>0?time:10;
        return time;
    }


    public long getTodayReadTime(Long uid) {
        String key = String.format(USER_READ_TIME_PREFIX, today(), uid);
        Object obj = redisCommands.sync().getRedisStringCommands().get(key);

        // Return Minutes
        Long millis = SafeConverter.toLong(obj);
        Long minutes=TimeUnit.MILLISECONDS.toMinutes(millis);

        return minutes;
    }



    public boolean isDoneReadPlan(Long pid,Long uid) {
        long readTime = getTodayReadTime(uid);
        int readPlan = getTodayReadPlan(pid,uid);
        return readTime > 0 && readPlan > 0 && readTime >= readPlan;
    }

    public void increTodayReadTime(Long uid, Long readMillis) {
        String key = String.format(USER_READ_TIME_PREFIX, today(), uid);
        redisCommands.sync().getRedisStringCommands().incrby(key, readMillis);
        redisCommands.sync().getRedisKeyCommands().expire(key, DAY_READ_EXPIRED_SECONDS);
    }


    public List<Long> getWeekReadTimeData(Long uid) {

        List<Long> list = new ArrayList<>();

        Map<String, Object> weekData = currentWeekDayInfo();
        List weekDays = (List) weekData.get("weekDays");
        int diff = SafeConverter.toInt(weekData.get("diff"));

        for (int i = 0; i < weekDays.size(); i++) {
            String dayName = String.valueOf(weekDays.get(i));
            long times = 0;
            // Avoid loop any useless request to redis
            if (i <= diff) {
                // Read from redis
                String key = String.format(USER_READ_TIME_PREFIX, dayName, uid);
                Object obj = redisCommands.sync().getRedisStringCommands().get(key);
                if (obj != null) {
                    Long millis = SafeConverter.toLong(obj);
                    // Return Minutes
                    times=TimeUnit.MILLISECONDS.toMinutes(millis);
                }
            } else {
                // Fill data,use default value
            }
            list.add(times);

        }
        return list;
    }


    public int getWeekContinuousCheckCount(Long uid) {

        Map<String, Object> weekData = currentWeekDayInfo();
        List weekDays = (List) weekData.get("weekDays");
        int diff = SafeConverter.toInt(weekData.get("diff"));

        int checkingCount = 0;
        for (int i = 0; i < weekDays.size(); i++) {
            String dayName = String.valueOf(weekDays.get(i));
            // Avoid loop some useless request to redis
            if (i <= diff) {
                // Read from redis
                String key = String.format(DAY_CHECK_PREFIX, dayName);
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


    public void addUserPushFormIds(Long pid, String formId) {
        String key = String.format(USER_PUSH_FORMIDS, pid);
        redisCommands.sync().getRedisStringCommands().set(key, formId);
    }

    public String getUserPushFormId(Long pid) {
        String key = String.format(USER_PUSH_FORMIDS, pid);
        Object id = redisCommands.sync().getRedisStringCommands().get(key);
        if (id != null) {
            return String.valueOf(id);
        }
        return "";
    }


    private String genWeekDayTimeKey() {
        String w = LocalDateTime.now().getDayOfWeek().toString();
        String t = LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
        return "MINI_PROGRAM_PUSH:" + w + "_" + t;

    }

    private String getWeekDayTimeKey(int dayOfWeek, String time) {
        String w = DayOfWeek.of(dayOfWeek).toString();
        String t = LocalTime.parse(time).truncatedTo(ChronoUnit.MINUTES).toString();
        return "MINI_PROGRAM_PUSH:" + w + "_" + t;


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
