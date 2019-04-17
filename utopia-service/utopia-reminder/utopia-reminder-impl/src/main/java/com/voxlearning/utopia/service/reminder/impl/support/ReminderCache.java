package com.voxlearning.utopia.service.reminder.impl.support;

import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.reminder.api.mapper.ReminderContext;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;
import com.voxlearning.utopia.service.reminder.constant.ReminderTarget;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.voxlearning.utopia.service.reminder.constant.ReminderPosition.*;

/**
 * @author shiwei.liao
 * @since 2017-5-10
 */
@Slf4j
@UtopiaCachePrefix(prefix = "ReminderCache")
public class ReminderCache extends PojoCacheObject<String, String> {

    private static final String LAST_UPDATE_TIME = "LAST_UPDATE_TIME";
    private static final String COUNTER = "COUNT";
    private static final String NUMBER_COUNTER = "NUMBER_COUNTER";
    private static final String TARGET_ID = "TARGET_ID";
    private static final String REMINDER_CONTENT = "REMINDER_CONTENT";

    private IRedisCommands redisCommands;

    public ReminderCache(UtopiaCache cache, IRedisCommands commands) {
        super(cache);
        redisCommands = commands;
    }

    //增加提醒
    public Boolean incr(ReminderTarget target, ReminderPosition position, String targetId, Boolean isNumber) {
        if (target == null || position == null || StringUtils.isBlank(targetId)) {
            return Boolean.FALSE;
        }
        String cacheKey = generateCacheKey(target, position, targetId);
        RedisHashAsyncCommands<String, Object> asyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        //计数器增加
        if (isNumber) {
            asyncCommands.hincrby(cacheKey, NUMBER_COUNTER, 1);
        } else {
            asyncCommands.hincrby(cacheKey, COUNTER, 1);
        }
        asyncCommands.hset(cacheKey, TARGET_ID, targetId);
        //设置最后更新日期
        asyncCommands.hset(cacheKey, LAST_UPDATE_TIME, Instant.now().toEpochMilli());
        //重置过期时间
        return cache.touch(cacheKey, generateExpireTime(position));
    }

    //增加带文案的提醒
    public Boolean incr(ReminderTarget target, ReminderPosition position, String targetId, String reminderContent) {
        if (target == null || position == null || StringUtils.isBlank(targetId)) {
            return Boolean.FALSE;
        }
        String cacheKey = generateCacheKey(target, position, targetId);
        RedisHashAsyncCommands<String, Object> asyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        //计数器增加
        asyncCommands.hincrby(cacheKey, COUNTER, 1);
        asyncCommands.hset(cacheKey, TARGET_ID, targetId);
        asyncCommands.hset(cacheKey, REMINDER_CONTENT, reminderContent);
        //设置最后更新日期
        asyncCommands.hset(cacheKey, LAST_UPDATE_TIME, Instant.now().toEpochMilli());
        //重置过期时间
        return cache.touch(cacheKey, generateExpireTime(position));
    }

    //减少提醒
    public Boolean decr(ReminderTarget target, ReminderPosition position, String targetId, Boolean isNumber) {
        if (target == null || position == null || StringUtils.isBlank(targetId)) {
            return Boolean.FALSE;
        }
        String cacheKey = generateCacheKey(target, position, targetId);
        RedisHashAsyncCommands<String, Object> asyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        //计数器增加
        if (isNumber) {
            asyncCommands.hincrby(cacheKey, NUMBER_COUNTER, -1);
        } else {
            asyncCommands.hincrby(cacheKey, COUNTER, -1);
        }
        asyncCommands.hset(cacheKey, TARGET_ID, targetId);
        //设置最后更新日期
        asyncCommands.hset(cacheKey, LAST_UPDATE_TIME, Instant.now().toEpochMilli());
        //重置过期时间
        return cache.touch(cacheKey, generateExpireTime(position));
    }

    //删除提醒
    public Boolean delete(ReminderTarget target, ReminderPosition position, String targetId) {
        if (target == null || position == null || StringUtils.isBlank(targetId)) {
            return Boolean.FALSE;
        }
        String cacheKey = generateCacheKey(target, position, targetId);
        return cache.delete(cacheKey);
    }

    //查询提醒
    public ReminderContext load(ReminderTarget target, ReminderPosition position, String targetId) {
        if (target == null || position == null || StringUtils.isBlank(targetId)) {
            return null;
        }
        String cacheKey = generateCacheKey(target, position, targetId);
        RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        Map<String, Object> cacheObject = hashCommands.hgetall(cacheKey);
        if (MapUtils.isEmpty(cacheObject)) {
            return null;
        }
        ReminderContext context = cacheObjectToContext(cacheObject);
        if (context == null) {
            return null;
        }
        context.setTarget(target);
        context.setPosition(position);
        return context;
    }

    //批量查询提醒=通过多个targetId
    public Map<String, ReminderContext> loads(ReminderTarget target, ReminderPosition position, Collection<String> targetIds) {
        if (target == null || position == null || CollectionUtils.isEmpty(targetIds)) {
            return Collections.emptyMap();
        }
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        List<RedisFuture<Map<String, Object>>> redisFutureList = new ArrayList<>();
        targetIds.forEach(targetId -> {
            String cacheKey = generateCacheKey(target, position, targetId);
            redisFutureList.add(hashAsyncCommands.hgetall(cacheKey));
        });
        Map<String, ReminderContext> map = new HashMap<>();
        for (RedisFuture<Map<String, Object>> future : redisFutureList) {
            try {
                Map<String, Object> cacheObject = future.get();
                ReminderContext context = cacheObjectToContext(cacheObject);
                if (context != null) {
                    context.setTarget(target);
                    context.setPosition(position);
                    map.put(context.getTargetId(), context);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("get user notify error,target is:{},position is:{},targetId is:{}", target.name(), position.name(), SafeConverter.toString(map.get(TARGET_ID)));
            }
        }
        return map;
    }

    //通过多个position批量查询
    public Map<ReminderPosition, ReminderContext> loads(ReminderTarget target, String targetId, Collection<ReminderPosition> positions) {
        if (target == null || StringUtils.isBlank(targetId) || CollectionUtils.isEmpty(positions)) {
            return Collections.emptyMap();
        }
        Set<ReminderPosition> loadPositions = new HashSet<>(positions);
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        Map<ReminderPosition, RedisFuture<Map<String, Object>>> redisFutureMap = new HashMap<>();
        loadPositions.forEach(position -> {
            String cacheKey = generateCacheKey(target, position, targetId);
            redisFutureMap.put(position, hashAsyncCommands.hgetall(cacheKey));
        });
        Map<ReminderPosition, ReminderContext> map = new HashMap<>();
        for (ReminderPosition position : redisFutureMap.keySet()) {
            RedisFuture<Map<String, Object>> future = redisFutureMap.get(position);
            try {
                Map<String, Object> cacheObject = future.get();
                ReminderContext context = cacheObjectToContext(cacheObject);
                if (context != null) {
                    context.setTarget(target);
                    context.setPosition(position);
                    map.put(position, context);
                }
            } catch (InterruptedException | ExecutionException e) {
                logger.error("get user notify error,target is:{},position is:{},targetId is:{}", target.name(), position.name(), SafeConverter.toString(targetId));
            }
        }
        return map;
    }

    //生成缓存键
    private String generateCacheKey(ReminderTarget target, ReminderPosition position, String targetId) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(position);
        Objects.requireNonNull(targetId);
        return cacheKey(target.name() + "_" + position.name() + "_" + targetId);
    }

    //计算过期时间
    private int generateExpireTime(ReminderPosition position) {
        Objects.requireNonNull(position);
        // TODO: 2017/8/3 等待书记重构
        List< ReminderPosition> positions = Arrays.asList(GW_XXLB, GW_TASK, GW_ISLAND_ENGLISH, GW_ISLAND_MATH,
                GW_ISLAND_CHINESE, GW_ISLAND_WISDOM, GW_MEDAL,PARENT_APP_EASEMOB_BOTTOM_MENU_NOTIFY,PARENT_APP_EASEMOB_INDEX_EXT, GW_LOTTO_TASK);
           if (positions.contains(position)) return DateUtils.getCurrentToDayEndSecond();

        int expire = position.getExpireDay();
        if (expire < 0) {
            expire = 1;
        }
        return 3600 * 24 * expire;
    }

    private ReminderContext cacheObjectToContext(Map<String, Object> cacheObject) {
        if (MapUtils.isEmpty(cacheObject)) {
            return null;
        }
        ReminderContext context = new ReminderContext();
        context.setTargetId(SafeConverter.toString(cacheObject.get(TARGET_ID)));
        //这里处理一下，因为操作都是kafka过来的。可能会出现为负数的情况。修正一下这个数据。
        // 至于缓存key里的值就不管了。业务自己保证调用decr的问题。
        int count = SafeConverter.toInt(cacheObject.get(COUNTER));
        count = count < 0 ? 0 : count;
        context.setReminderCount(count);
        int numberCount = SafeConverter.toInt(cacheObject.get(NUMBER_COUNTER));
        numberCount = numberCount < 0 ? 0 : numberCount;
        context.setReminderNumCount(numberCount);
        Long update = SafeConverter.toLong(cacheObject.get(LAST_UPDATE_TIME));
        if (update > 0) {
            context.setLastUpdateDate(new Date(update));
        }
        context.setReminderContent(SafeConverter.toString(cacheObject.get(REMINDER_CONTENT)));
        return context;
    }

}
