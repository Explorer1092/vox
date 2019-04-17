package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 学生分班缓存
 *
 * @author songtao
 * @since 2018/06/25
 */
@Named
public class UserVirtualClazzCacheManager implements InitializingBean {

    private IRedisCommands redisCommands;

    private static String CLAZZ1_KEY = "Chips:UserVirtualClazzCacheManager:CID:1";

    private static String CLAZZ2_KEY = "Chips:UserVirtualClazzCacheManager:CID:2";

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("chips-redis");
    }

    public synchronized void addRecord(Long userId) {
        String clazzKey = getClazzKey();
        redisCommands.sync().getRedisSetCommands().sadd(clazzKey, userId);
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expireat(clazzKey, DateUtils.addDays(new Date(), 60));
    }

    public void removeRecord(Long userId) {
        redisCommands.sync().getRedisSetCommands().srem(CLAZZ1_KEY, userId);
        redisCommands.sync().getRedisSetCommands().srem(CLAZZ2_KEY, userId);
    }

    public Set<Long> loadClazz1Record() {
        return getRecord(CLAZZ1_KEY);
    }

    public Set<Long> loadClazz2Record() {
        return getRecord(CLAZZ2_KEY);
    }

    private Set<Long> getRecord(String cacheKey) {
        Set<Object> res = redisCommands.sync().getRedisSetCommands().smembers(cacheKey);
        if (CollectionUtils.isEmpty(res)) {
            return Collections.emptySet();
        }

        Set<Long> studentSet = new HashSet<>();
        res.forEach(e -> {
            studentSet.add(SafeConverter.toLong(e));
        });
        return studentSet;
    }
    private String getClazzKey() {
//        Set<Object> res = redisCommands.sync().getRedisSetCommands().smembers(CLAZZ1_KEY);
//        if (CollectionUtils.isEmpty(res) || res.size() <= (RuntimeMode.le(Mode.STAGING) ? 10 : 400)) {
//            return CLAZZ1_KEY;
//        }
        return CLAZZ2_KEY;
    }
}
