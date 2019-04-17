package com.voxlearning.utopia.service.reminder.impl.support;

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
 * @since 2017-5-10
 */
@Named
public class ReminderCacheSystem extends SpringContainerSupport {

    @Getter
    private ReminderCache reminderCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CacheBuilder cacheBuilder = CacheSystem.RDS.getCacheBuilder();
        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        IRedisCommands reminderRedisCommands = instance.getRedisCommands("message-reminder");
        UtopiaCache reminderCache = cacheBuilder.getCache("message-reminder");
        this.reminderCache = new ReminderCache(reminderCache,reminderRedisCommands);
    }
}
