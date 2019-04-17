package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Date;

/**
 * 缓存小程序token
 *
 * @author songtao
 */
@Named
public class ChipsMiniProgramTokenCacheManager implements InitializingBean {

    private IRedisCommands redisCommands;

    private static String CATHE_KEY = "ChipsMiniProgramTokenCacheManager:token";

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("chips-redis");
    }


    public void addRecord(String token, int seconds) {
        redisCommands.sync().getRedisStringCommands().set(CATHE_KEY, token);
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expireat(CATHE_KEY, DateUtils.addSeconds(new Date(), seconds - 10));
    }


    public String load() {
        return SafeConverter.toString(redisCommands.sync().getRedisStringCommands().get(CATHE_KEY));
    }
}
