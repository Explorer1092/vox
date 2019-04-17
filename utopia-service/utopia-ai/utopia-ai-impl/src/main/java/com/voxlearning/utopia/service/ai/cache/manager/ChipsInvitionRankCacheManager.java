package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.ai.data.ChipsRank;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Named
public class ChipsInvitionRankCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_INVITION_COUNT_RANK_";
    private static final Long EXPIRE_TIME = 30 * 24 * 60 * 60L;     // 设置为30天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long updateRank(String activityType, Long userId, int number) {
        if (userId == null || StringUtils.isBlank(activityType)) {
            return null;
        }
        String key = key(activityType);
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        sortedSetCommands.zaddincr(key, number, userId);
        return sortedSetCommands.zrevrank(key, userId) + 1;
    }

    /**
     * ChipsRank 只有userId，number有值
     *
     * @param activityType
     * @param limit
     * @return
     */
    public List<ChipsRank> getRankList(String activityType, Integer limit) {
        if (limit == null) {
            return Collections.emptyList();
        }
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        String key = key(activityType);
        List<ScoredValue<Object>> scoredValues = sortedSetCommands.zrevrangeWithScores(key, 0, limit);
        if (CollectionUtils.isEmpty(scoredValues)) {
            return Collections.emptyList();
        }
        return scoredValues.stream().filter(e -> e.value != null && StringUtils.isNotBlank(e.value.toString())).filter(e -> e.score > 10).map(e -> {
            ChipsRank rank = new ChipsRank();
            rank.setUserId(Long.valueOf(e.value.toString()));
            Integer number = new BigDecimal(e.score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            rank.setNumber(number);
            return rank;
        }).collect(Collectors.toList());
    }

    public void reset(String activityType) {
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.del(key(activityType));
    }

    private String key(String activityType) {
        return READ_KEY + activityType;
    }
}
