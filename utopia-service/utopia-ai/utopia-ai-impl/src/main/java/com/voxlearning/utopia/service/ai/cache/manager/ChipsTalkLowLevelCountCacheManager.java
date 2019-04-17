package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 薯条对话
 */
@Named
public class ChipsTalkLowLevelCountCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_ENGLISH_TALK_LOW_LEVEL_";
    private static final Long EXPIRE_TIME =  2 * 60 * 60L;     // 设置为2个小时

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long increase(String qid, Long userId) {
        String key = key(qid, userId);
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);
        return commands.incr(key);
    }

    private String key(String qid, Long userId) {
        return READ_KEY + qid + "_" + userId;
    }

    public void delete(String qid, Long userId) {
        String key = key(qid, userId);
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.del(key);
    }

    public void delete(List<String> qids, Long userId) {
        Set<String> keys = new HashSet<>();
        qids.forEach(q -> keys.add(key(q, userId)));
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.del(keys.toArray(new String[0]));
    }
}
