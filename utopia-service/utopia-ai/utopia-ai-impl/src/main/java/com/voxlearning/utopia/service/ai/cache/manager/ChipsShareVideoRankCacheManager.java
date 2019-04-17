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
import java.util.List;

/**
 * 分享的视频观看榜
 */
@Named
public class ChipsShareVideoRankCacheManager extends SpringContainerSupport {

    private static final String READ_KEY = "CHIPS_VIDEO_SHARE_";
    private static final Long EXPIRE_TIME = 2 * 24 * 60 * 60L;     // 设置为2天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("chips-redis");
    }

    public Long updateRank(Long userId, String unitId, String clazz,Long total) {
        if (userId == null || total == null) {
            return null;
        }

        String key = key(clazz, unitId);
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        sortedSetCommands.zaddincr(key, total, userId);
        if (keyCommands.ttl(key) == -1) {
            keyCommands.expire(key, EXPIRE_TIME);
        }
        return sortedSetCommands.zrevrank(key, userId) + 1;
    }

    public List<ChipsRank> getRankList(Integer limit,  String unitId, String clazz) {
        if (limit == null) {
            return Collections.emptyList();
        }

        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();

        String key = key(clazz, unitId);
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
        rankList.sort((e1, e2) -> e2.getNumber().compareTo(e1.getNumber()));
        return rankList;
    }

    private String key(String clazz, String unitId) {
        return READ_KEY + clazz + unitId;
    }
}
