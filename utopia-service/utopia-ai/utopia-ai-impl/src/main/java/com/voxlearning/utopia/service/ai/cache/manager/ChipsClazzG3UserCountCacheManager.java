package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;

/**
 * 薯条班级grade3 定级计数器
 */
@Named
public class ChipsClazzG3UserCountCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_ENGLISH_CLASS_GRADE_3_USER_COUNT_";
    private static final Long EXPIRE_TIME = 60 * 24 * 60 * 20L;     // 设置为60天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long increase(Long clazzId) {
        String key = key(clazzId);
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        return commands.incr(key);
    }

    private String key(Long clazzId) {
        return READ_KEY + clazzId;
    }
}
