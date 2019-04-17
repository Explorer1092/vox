package com.voxlearning.utopia.service.ai.cache.manager;

import com.lambdaworks.redis.Range;
import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.ai.data.ChipsRank;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
@Named
public class ChipsGradeRankCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_GRADE_SCORE_RANK_";
    private static final Long EXPIRE_TIME = 12 * 24 * 60 * 60L;     // 设置为12天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long updateRank(Long userId, Long clazz, int score) {
        if (userId == null || clazz == null) {
            return null;
        }

        String key = key(clazz);
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.expire(key, EXPIRE_TIME);

        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        sortedSetCommands.zaddincr(key, score, userId);

        return sortedSetCommands.zrevrank(key, userId) + 1;
    }

    public List<ChipsRank> getRankList(Long clazz, Integer limit) {
        if (limit == null) {
            return Collections.emptyList();
        }

        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();

        String key = key(clazz);
        Double topScore = null;
        List<ScoredValue<Object>> scoredValues = sortedSetCommands.zrevrangeWithScores(key, 0, 0);
        if (CollectionUtils.isNotEmpty(scoredValues)) {
            topScore = scoredValues.get(0).score;
        }
        if (topScore == null) {
            return Collections.emptyList();
        }
        Double lowerScore = null;
        scoredValues = sortedSetCommands.zrevrangeWithScores(key, limit - 1, limit - 1);
        if (CollectionUtils.isNotEmpty(scoredValues)) {
            lowerScore = scoredValues.get(0).score;
        }

        if (lowerScore == null) {
            scoredValues = sortedSetCommands.zrevrangeWithScores(key, 0, limit - 1);
        } else {
            Range<Double> range = Range.from(Range.Boundary.including(lowerScore), Range.Boundary.including(topScore));
            scoredValues = sortedSetCommands.zrevrangebyscoreWithScores(key, range);
        }

        if (CollectionUtils.isEmpty(scoredValues)) {
            return Collections.emptyList();
        }

        // 重新计算排名
        List<ChipsRank> rankList = new ArrayList<>();
        for (ScoredValue<Object> scoredValue : scoredValues) {
            Integer totalScoreValue = new BigDecimal(scoredValue.score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            rankList.add(ChipsRank.newInstance(SafeConverter.toLong(scoredValue.value), totalScoreValue));
        }
        rankList.sort(Comparator.comparing(ChipsRank::getNumber));
        return rankList;
    }

    public void reset(Long clazz) {
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        keyCommands.del(key(clazz));
    }

    private String key(Long clazz) {
        return READ_KEY + clazz;
    }
}
