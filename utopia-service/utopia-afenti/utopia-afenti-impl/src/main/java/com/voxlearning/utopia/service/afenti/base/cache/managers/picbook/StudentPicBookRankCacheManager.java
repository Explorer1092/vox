package com.voxlearning.utopia.service.afenti.base.cache.managers.picbook;

import com.lambdaworks.redis.Range;
import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import com.voxlearning.utopia.service.afenti.api.data.PicBookRankInfo;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Summer on 2018/4/3
 * 绘本阅读成就全国周榜
 */
@Named
public class StudentPicBookRankCacheManager extends SpringContainerSupport {

    // 全国榜key
    private static final String READ_KEY = "PIC_BOOK_STUDENT_READ_RANK_";
    private static final String WORD_KEY = "PIC_BOOK_STUDENT_WORD_RANK_";
    private static final Long EXPIRE_TIME = 60 * 24 * 60 * 16L;     // 设置为16天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("picbook-redis");
    }

    public Long updateStudentRank(Long studentId, Integer total, PicBookRankCategory rankType, Integer week) {
        if (studentId == null || total == null || rankType == null || week == null) {
            return null;
        }
        String key = getKey(rankType, week);
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        sortedSetCommands.zadd(key, total, studentId);
        if (keyCommands.ttl(key) == -1) {
            keyCommands.expire(key, EXPIRE_TIME);
        }
        return sortedSetCommands.zrevrank(key, studentId) + 1;
    }

    private String getKey(PicBookRankCategory rankType, Integer week) {
        if (rankType == PicBookRankCategory.READ) {
            return READ_KEY + week;
        } else if (rankType == PicBookRankCategory.WORD) {
            return WORD_KEY + week;
        }
        return null;
    }

    public PicBookRankInfo getStudentRankByStudentIdAndRankType(Long studentId, PicBookRankCategory rankType, Integer week) {
        if (rankType == null || studentId == null || week == null) {
            return null;
        }

        String key = getKey(rankType, week);
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();

        // 获取个人排名
        Long rank = sortedSetCommands.zrevrank(key, studentId);
        Double score = sortedSetCommands.zscore(key, studentId);
        if (rank == null || score == null) {
            return null;
        }

        if (rank <= 100) {
            // 小于100时获取真实带并列的排名
            Double topScore = null;
            List<ScoredValue<Object>> scoredValues = sortedSetCommands.zrevrangeWithScores(key, 0, 0);
            if (CollectionUtils.isNotEmpty(scoredValues)) {
                topScore = scoredValues.get(0).score;
            }

            if (topScore == null) {
                return null;
            }
            Range<Double> range = Range.from(Range.Boundary.excluding(score), Range.Boundary.including(topScore));
            rank = sortedSetCommands.zcount(key, range) + 1;
        } else
            rank = rank + 1;

        return PicBookRankInfo.newInstanceForRank(studentId, score.intValue(), SafeConverter.toInt(rank), rankType);
    }


    public List<PicBookRankInfo> getRankList(Integer limit, PicBookRankCategory rankType, Integer week) {
        if (rankType == null || limit == null || week == null) {
            return Collections.emptyList();
        }

        String key = getKey(rankType, week);
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();

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
        List<PicBookRankInfo> rankList = new ArrayList<>();
        int rankIndex = 0;
        int rankCount = 0;
        int tempTotalGrowUpValue = -1;

        for (ScoredValue<Object> scoredValue : scoredValues) {
            rankCount++;
            Integer totalScoreValue = new BigDecimal(scoredValue.score).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            if (tempTotalGrowUpValue != totalScoreValue) {
                // 总值不同时名次增加，反之名次并列
                tempTotalGrowUpValue = totalScoreValue;
                rankIndex = rankCount;
            }
            // 加入实际排名
            rankList.add(PicBookRankInfo.newInstanceForRank(SafeConverter.toLong(scoredValue.value), totalScoreValue, rankIndex, rankType));
        }
        return rankList;
    }
}
