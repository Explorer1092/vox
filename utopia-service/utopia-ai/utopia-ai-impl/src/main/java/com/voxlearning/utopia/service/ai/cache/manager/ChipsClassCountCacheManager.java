package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;

/**
 * 薯条班级用户计数器
 */
@Named
public class ChipsClassCountCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_ENGLISH_CLASS_COUNT_";
    private static final Long EXPIRE_TIME = 60 * 24 * 60 * 60L;     // 设置为60天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long increase(Long clazzId) {
        String key = key(clazzId);
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);
        return commands.incr(key);
    }

    public void decrease(Long clazzId) {
        String key = key(clazzId);
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);
        commands.decr(key);
    }

    private String key(Long clazzId) {
        return READ_KEY + clazzId;
    }
}
