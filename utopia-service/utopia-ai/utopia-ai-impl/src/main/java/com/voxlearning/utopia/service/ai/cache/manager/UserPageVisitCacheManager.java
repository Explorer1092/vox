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

@Named
public class UserPageVisitCacheManager implements InitializingBean {

    private IRedisCommands redisCommands;

    private static String CATHE_KEY = "Chips:UserPageVisitCacheManager:page:";

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("chips-redis");
    }

    public void addRecord(Long id, String page) {
        addRecord(id, page, 20);
    }

    public void addRecord(Long id, String page, int days) {
        String key = key(page);
        redisCommands.sync().getRedisSetCommands().sadd(key, id);
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expireat(key, DateUtils.addDays(new Date(), days));
    }

    public Set<Long> getRecordIds(String page) {
        Set<Object> res = redisCommands.sync().getRedisSetCommands().smembers(key(page));
        if (CollectionUtils.isEmpty(res)) {
            return Collections.emptySet();
        }

        Set<Long> studentSet = new HashSet<>();
        res.forEach(e -> {
            studentSet.add(SafeConverter.toLong(e));
        });
        return studentSet;
    }

    public void delete(String page) {
        String key = key(page);
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.del(key);
    }


    private String key(String page) {
        return CATHE_KEY + page;
    }
}
