package com.voxlearning.utopia.service.afenti.base.cache.managers;

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
 * 缓存期末复习家庭参与值班级排名
 *
 * @author songtao
 * @since 2017/11/30
 */
@Named("com.voxlearning.utopia.service.afenti.base.cache.managers.AfentiReviewFamilyClassRankingCacheManager")
public class AfentiReviewFamilyClassRankingCacheManager implements InitializingBean {

    private IRedisCommands redisCommands;

    private static String KEY_PREFIX = "AfentiReviewFamilyClassRankingCacheManager:CID:";

    @Override
    public void afterPropertiesSet() throws Exception {
        RedisCommandsBuilder builder = RedisCommandsBuilder.getInstance();
        this.redisCommands = builder.getRedisCommands("user-easemob");
    }

    public void addRecord(Long clazzId, Long studentId) {
        String cacheKey = buildKey(clazzId);
        redisCommands.sync().getRedisSetCommands().sadd(cacheKey, studentId);
        RedisKeyCommands<String,Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expireat(cacheKey, DateUtils.addDays(new Date(), 60));
    }

    public Set<Long> loadRecord(Long clazzId) {
        String cacheKey = buildKey(clazzId);
        Set<Object> res = redisCommands.sync().getRedisSetCommands().smembers(cacheKey);
        if (CollectionUtils.isEmpty(res)) {
            return Collections.emptySet();
        }

        Set<Long> studentSet = new HashSet<>();
        res.forEach(e -> {
            studentSet.add(SafeConverter.toLong(e));
        });

        return studentSet;
    }

    public void deleteRecord(Long clazzId) {
        String cacheKey = buildKey(clazzId);
        redisCommands.sync().getRedisKeyCommands().del(cacheKey);
    }

    private String buildKey(Long clazzId) {
        return KEY_PREFIX + clazzId;
    }

}
