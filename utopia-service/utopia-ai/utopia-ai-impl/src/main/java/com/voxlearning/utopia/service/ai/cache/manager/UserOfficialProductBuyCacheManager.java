package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 学生购买正式课程缓存
 *
 * @author songtao
 * @since 2018/07/16
 */
@Named
public class UserOfficialProductBuyCacheManager implements InitializingBean {

    private IRedisCommands redisCommands;

    private static String CATHE_KEY = "Chips:UserOfficialProductBuyCacheManager:GRADE:";

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("chips-redis");
    }

    public void addRecord(Long userId, Integer grade) {
        String key = key(grade);
        redisCommands.sync().getRedisSetCommands().sadd(key, userId);
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expireat(key, DateUtils.addDays(new Date(), 100));
    }


    public Set<Long> getRecord(Integer grade) {
        Set<Object> res = redisCommands.sync().getRedisSetCommands().smembers(key(grade));
        if (CollectionUtils.isEmpty(res)) {
            return Collections.emptySet();
        }

        Set<Long> studentSet = new HashSet<>();
        res.forEach(e -> {
            studentSet.add(SafeConverter.toLong(e));
        });
        return studentSet;
    }

    private String key(Integer grade) {
        return CATHE_KEY + grade;
    }
}
