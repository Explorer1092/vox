package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.List;

/**
 * 薯条产品用户计数器
 */
@Named
public class ChipsProductUserCountCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_ENGLISH_CLASS_USER_COUNT_";
    private static final Long EXPIRE_TIME = 60 * 24 * 60 * 60L;     // 设置为60天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long increase(String productId) {
        String key = key(productId);
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);
        return commands.incr(key);
    }

    public Long getCount(String productId) {
        String key = key(productId);
        RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
        return SafeConverter.toLong(commands.get(key));
    }

    public Long getTotalCount(List<String> productIds) {
        long res = 0L;
        for(String product :  productIds) {
            String key = key(product);
            RedisStringCommands<String, Object> commands = redisCommands.sync().getRedisStringCommands();
            res += SafeConverter.toLong(commands.get(key));
        }
        return res;
    }


    private String key(String productId) {
        return READ_KEY + productId;
    }
}
