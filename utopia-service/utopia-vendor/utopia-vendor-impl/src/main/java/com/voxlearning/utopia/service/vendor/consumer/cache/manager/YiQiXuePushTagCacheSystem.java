package com.voxlearning.utopia.service.vendor.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.CacheBuilder;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-11-27
 */
@Named
public class YiQiXuePushTagCacheSystem extends SpringContainerSupport {

    @Getter
    private YiQiXuePushTagCache yiQiXuePushTagCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CacheBuilder cacheBuilder = CacheSystem.RDS.getCacheBuilder();
        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        IRedisCommands yiQiXueRedisCommands = instance.getRedisCommands("parent-app");
        UtopiaCache yiQiXueCache = cacheBuilder.getCache("parent-app");
        this.yiQiXuePushTagCache = new YiQiXuePushTagCache(yiQiXueCache, yiQiXueRedisCommands);
    }
}
