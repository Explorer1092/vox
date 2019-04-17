package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.async.RedisSortedSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadCollection;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadLikeCountScoreMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author jiangpeng
 * @since 2017-03-24 下午5:38
 **/

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 7)
@UtopiaCacheRevision(value = "20170410")
public class FollowReadShareLikeRankCacheManager extends PojoCacheObject<String, String> {

    private IRedisCommands redisCommands;

    public FollowReadShareLikeRankCacheManager(UtopiaCache cache, IRedisCommands redisCommands) {
        super(cache);
        this.redisCommands = redisCommands;
    }

    /**
     * 城市排行榜
     * @param range weekRange   dayRange
     * @param collectionId 作品id
     * @param cityId  城市id
     * @param delta  增加赞数量
     * @return 赞数量
     */
    public Long cityRangeIncrLike(DateRange range, String collectionId, Integer cityId, Long delta, boolean weekRank) {
        String key = generateCacheKey(range, "city", cityId, weekRank);
        return incrLike(key, collectionId, delta);
    }

    public void cityRangeSetLike(DateRange range, String collectionId, Integer cityId, long likeCount, Date shareCreateTime, boolean weekRank) {
        String key = generateCacheKey(range, "city", cityId, weekRank);
        FollowReadLikeCountScoreMapper likeScore = new FollowReadLikeCountScoreMapper(likeCount, range, shareCreateTime);
        RedisSortedSetCommands<String, Object> redisSortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        redisSortedSetCommands.zadd(key, likeScore.toScore(), collectionId);
    }

    public List<FollowReadLikeCountScoreMapper> getCityLikeRangeList(DayRange dayRange, Integer cityId, Integer topNum, boolean weekRank){
        return rangeMapperList(dayRange, "city", cityId, topNum, weekRank);
    }



    public Long schoolRangeIncrLike(DateRange range, String collectionId, Long schoolId, Long delta, boolean weekRank) {
        String key = generateCacheKey(range, "school", schoolId, weekRank);
        return incrLike(key, collectionId, delta);

    }
    public void schoolRangeSetLike(DateRange range, String collectionId, Long schoolId, long likeCount, Date shareCreateTime, boolean weekRank) {
        String key = generateCacheKey(range, "school", schoolId, weekRank);
        FollowReadLikeCountScoreMapper likeScore = new FollowReadLikeCountScoreMapper(likeCount, range, shareCreateTime);
        RedisSortedSetAsyncCommands<String, Object> sortedSetAsyncCommands = redisCommands.async().getRedisSortedSetAsyncCommands();
        sortedSetAsyncCommands.zadd(key, likeScore.toScore(), collectionId);
    }
    public List<FollowReadLikeCountScoreMapper> getSchoolLikeRangeList(DayRange dayRange, Long schoolId, Integer topNum, boolean weekRank){
        return rangeMapperList(dayRange, "school", schoolId, topNum, weekRank);

    }


    public Long globalRangeIncrLike(DateRange range, String collectionId, Long delta, boolean weekRank) {
        String key = generateCacheKey(range, "global", null, weekRank);
        return incrLike(key, collectionId, delta);
    }

    public void globalRangeSetLike(DateRange range, String collectionId, long likeCount, Date shareCreateTime, boolean weekRank) {
        String key = generateCacheKey(range, "global", null, weekRank);
        FollowReadLikeCountScoreMapper likeScore = new FollowReadLikeCountScoreMapper(likeCount, range, shareCreateTime);
        RedisSortedSetAsyncCommands<String, Object> sortedSetAsyncCommands = redisCommands.async().getRedisSortedSetAsyncCommands();
        sortedSetAsyncCommands.zadd(key, likeScore.toScore(), collectionId);
    }

    public List<FollowReadLikeCountScoreMapper> getGlobalLikeRangeList(DayRange dayRange, Integer topNum, boolean weekRank) {
        return rangeMapperList(dayRange, "global", null, topNum, weekRank);
    }

    public void deleteSchoolLikeRangeMore(DateRange range, Long schoolId, Integer topNum, boolean weekRank){
        String key = generateCacheKey(range, "school", schoolId, weekRank);
        deleteRangeMore(key, topNum);
    }

    public void deleteCityLikeRangeMore(DateRange range, Integer cityId, Integer topNum, boolean weekRank){
        String key = generateCacheKey(range, "city", cityId, weekRank);
        deleteRangeMore(key, topNum);
    }

    public void deleteGlobalLikeRangeMore(DateRange range, Integer topNum, boolean weekRank){
        String key = generateCacheKey(range, "global", null, weekRank);
        deleteRangeMore(key, topNum);
    }

    private void deleteRangeMore(String key, Integer topNum){
        RedisSortedSetAsyncCommands<String, Object> sortedSetAsyncCommands = redisCommands.async().getRedisSortedSetAsyncCommands();
        RedisFuture<Long> zcard = sortedSetAsyncCommands.zcard(key);
        Long totalMemberCount = getFutureValue(zcard, 0L);
        if (totalMemberCount <= topNum)
            return;
        sortedSetAsyncCommands.zremrangebyrank(key, 0, totalMemberCount - topNum -1);
    }

    private <T> T getFutureValue(Future<T> future, T defaultValue){
        try {
            return future.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }


    /**
     *
     * @param topNum 取前xx名
     * @return
     */
    private List<FollowReadLikeCountScoreMapper> rangeMapperList(DayRange range, String dimension, Object dimensionId, Integer topNum, boolean weekRank){
        RedisSortedSetCommands<String, Object> redisSortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        List<FollowReadLikeCountScoreMapper> list = new ArrayList<>(topNum);
        List<ScoredValue<Object>> scoredValues;
        String key = generateCacheKey(range, dimension, dimensionId, weekRank);
        scoredValues = redisSortedSetCommands.zrevrangeWithScores(key, 0, topNum-1);
        scoredValues.forEach(t -> {
            String collectionId = SafeConverter.toString(t.value);
            if (collectionId == null || !FollowReadCollection.validateId(collectionId))
                return;
            double score = t.score;
            FollowReadLikeCountScoreMapper mapper = FollowReadLikeCountScoreMapper.instantsFromScore(score, collectionId);
            if (mapper != null) {
                list.add(mapper);
            }
        });
        //排行榜看过后的当天最后时刻过期
        redisCommands.async().getRedisKeyAsyncCommands().expireat(key, DayRange.current().getEndDate());
        return list;
    }

    private Long incrLike(String key, String collectionId, Long delta){
        RedisSortedSetCommands<String, Object> redisSortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        Double resultScore = redisSortedSetCommands.zaddincr(key, FollowReadLikeCountScoreMapper.toScoreWithoutTime(delta), collectionId);
        return FollowReadLikeCountScoreMapper.getLikeCountFromScore(resultScore);
    }


    private String generateCacheKey(DateRange range, String dimension, Object dimensionId, boolean weekRank) {
        return cacheKey(String.format("%s-%s-%s", range.toString(), weekRank ? dimension + "-week" : dimension, dimensionId == null ? "" : dimensionId.toString()));
    }
}
