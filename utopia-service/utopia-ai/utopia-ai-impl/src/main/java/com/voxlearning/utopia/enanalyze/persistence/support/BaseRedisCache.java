package com.voxlearning.utopia.enanalyze.persistence.support;

import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.utopia.enanalyze.persistence.RedisConstant;
import org.springframework.beans.factory.InitializingBean;

/**
 * redis缓存
 *
 * @author xiaolei.li
 * @version 2018/7/24
 */
public class BaseRedisCache implements InitializingBean {

    /**
     * redis命令
     */
    protected IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() {
        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        redisCommands = instance.getRedisCommands(RedisConstant.CONFIG);
    }
}
