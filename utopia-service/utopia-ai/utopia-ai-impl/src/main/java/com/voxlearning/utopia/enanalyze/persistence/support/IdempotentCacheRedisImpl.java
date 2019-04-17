package com.voxlearning.utopia.enanalyze.persistence.support;

import com.lambdaworks.redis.api.sync.RedisStringCommands;
import com.voxlearning.utopia.enanalyze.persistence.IdempotentCache;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import org.springframework.stereotype.Repository;

/**
 * 幂等缓存redis实现
 *
 * @author xiaolei.li
 * @version 2018/7/25
 */
@Repository
public class IdempotentCacheRedisImpl extends BaseRedisCache implements IdempotentCache {
    @Override
    public boolean exist(String key) {
        Long exists = redisCommands.sync().getRedisKeyCommands().exists(new String[]{RedisConstant.Key.IDEMPOTENT.getKey() + ":" + key});
        return Long.valueOf(1).equals(exists);
    }

    @Override
    public String get(String key) {
        RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
        return (String) stringCommands.get(RedisConstant.Key.IDEMPOTENT.getKey() + ":" + key);
    }

    @Override
    public void set(String key, String value, long expire) {
        RedisStringCommands<String, Object> stringCommands = redisCommands.sync().getRedisStringCommands();
        stringCommands.setex(RedisConstant.Key.IDEMPOTENT.getKey() + ":" + key, expire, value);
    }
}
