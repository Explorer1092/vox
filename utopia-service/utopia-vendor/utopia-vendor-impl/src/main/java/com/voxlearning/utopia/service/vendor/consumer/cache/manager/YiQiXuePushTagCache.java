package com.voxlearning.utopia.service.vendor.consumer.cache.manager;

import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;

/**
 * @author shiwei.liao
 * @since 2017-11-27
 */
@Slf4j
public class YiQiXuePushTagCache extends PojoCacheObject<String, String> {

    private static final String TAG = "YI_QI_XUE_TAG";

    private IRedisCommands redisCommands;

    public YiQiXuePushTagCache(UtopiaCache cache, IRedisCommands commands) {
        super(cache);
        this.redisCommands = commands;
    }


    public Boolean setUserYiQiXuePushTag(Long userId, Set<String> tags) {
        if (userId == null) {
            return Boolean.FALSE;
        }
        if (tags == null) {
            tags = Collections.emptySet();
        }
        String cacheKey = cacheKey(userId.toString());
        RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        hashCommands.hset(cacheKey, TAG, tags);
        return Boolean.TRUE;
    }

    public Set<String> loadUserYiQiXuePushTag(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        String cacheKey = cacheKey(userId.toString());
        RedisHashCommands<String, Object> hashCommands = redisCommands.sync().getRedisHashCommands();
        Object cacheObject = hashCommands.hget(cacheKey, TAG);
        if (cacheObject == null) {
            return Collections.emptySet();
        }
        return (Set) (cacheObject);
    }
}
