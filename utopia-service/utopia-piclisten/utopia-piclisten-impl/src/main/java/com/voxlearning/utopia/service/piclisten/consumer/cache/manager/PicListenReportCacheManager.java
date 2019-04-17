package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.async.RedisSetAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.lambdaworks.redis.api.sync.RedisSetCommands;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenReportDayResult;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 点读机报告
 *
 * @author jiangpeng
 * @since 2017-03-15 下午4:43
 **/
@UtopiaCachePrefix(prefix = "plReport")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400 * 8)
public class PicListenReportCacheManager extends PojoCacheObject<String, String> {


    private final static String SCORE_RESULT_KEY_PREFIX = "score";
    private final static String SCORE_RESULT_READ_SENTENCE_SET_PREFIX = "sen_set";
    private final static String DAY_ACTIVE_STUDENT_ID_PREFIX = "active_sid";

    private final static String LEARN_TIME = "l_t";
    private final static String PICLISTEN_SENTENCE_COUNT = "pl_s_c";
    private final static String FOLLOW_READ_SENTENCE_COUNT = "fr_s_c";
    private final static String REPORTE_SCORE = "r_sc";
    private final static String HAS_FOLLOW_READ = "h_fr";


    private IRedisCommands redisCommands;

    public PicListenReportCacheManager(UtopiaCache cache, IRedisCommands redisCommands) {
        super(cache);
        this.redisCommands = redisCommands;
    }

    public void studentDayActiveFollowRead(DayRange dayRange, Long studentId){
        String key = generateDayActiveStudentIdSetKey(dayRange);
        RedisSetCommands<String, Object> redisSetCommands = redisCommands.sync().getRedisSetCommands();
        if (redisSetCommands.sismember(key, studentId))
            return;
        Long sadd = redisSetCommands.sadd(key, studentId);
        if (sadd == 1)
            getCache().touch(key, 86400 * 30);
    }
    public Set<Long> dayActiveFollowReadStudentIdSet(DayRange dayRange){
        String key = generateDayActiveStudentIdSetKey(dayRange);
        RedisSetCommands<String, Object> redisSetCommands = redisCommands.sync().getRedisSetCommands();
        Set<Object> smembers = redisSetCommands.smembers(key);
        return smembers.stream().map(SafeConverter::toLong).collect(Collectors.toSet());
    }

    public Map<String, Future<Boolean>> asyncSentencesIsRead(Long studentId, String dayRange, Collection<String> sentenceIds, String type){
        if (CollectionUtils.isEmpty(sentenceIds))
            return Collections.emptyMap();
        String key = generateKey(SCORE_RESULT_READ_SENTENCE_SET_PREFIX, studentId, dayRange, type);
        RedisSetAsyncCommands<String, Object> redisSetAsyncCommands = redisCommands.async().getRedisSetAsyncCommands();
        Map<String, Future<Boolean>> resultFutureMap = new HashMap<>();
        for (String sentenceId : sentenceIds) {
            resultFutureMap.put(sentenceId, redisSetAsyncCommands.sismember(key, sentenceId));
        }
        return resultFutureMap;
    }

    public void asyncAddListenedSentence(Long studentId, String dayRange, Set<String> sentenceIdSet, String type){
        if (CollectionUtils.isEmpty(sentenceIdSet))
            return;
        String key = generateKey(SCORE_RESULT_READ_SENTENCE_SET_PREFIX, studentId, dayRange, type);
        RedisSetAsyncCommands<String, Object> redisSetAsyncCommands = redisCommands.async().getRedisSetAsyncCommands();
        redisSetAsyncCommands.sadd(key, sentenceIdSet.toArray());
        cache.touch(key ,expirationInSeconds());
    }

    public Long asyncUpdateScoreResult(Long studentId, String dayRange, Long learnTime, Long picListenSentenceCount,
                                       Long followReadSentenceCount, Long reportScore, Boolean hasFollowRead){
        RedisFuture<Long> future = null;
        String key = generateKey(SCORE_RESULT_KEY_PREFIX, studentId, dayRange, "");
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        if (learnTime != null && learnTime > 0) {
            future = hashAsyncCommands.hincrby(key, LEARN_TIME, learnTime);
        }
        if (picListenSentenceCount != null && picListenSentenceCount > 0)
            hashAsyncCommands.hincrby(key, PICLISTEN_SENTENCE_COUNT, picListenSentenceCount);
        if (followReadSentenceCount != null && followReadSentenceCount > 0)
            hashAsyncCommands.hincrby(key, FOLLOW_READ_SENTENCE_COUNT, followReadSentenceCount);
        if (reportScore != null && reportScore > 0)
            hashAsyncCommands.hset(key, REPORTE_SCORE, SafeConverter.toString(reportScore));
        if (hasFollowRead != null)
            hashAsyncCommands.hset(key, HAS_FOLLOW_READ, SafeConverter.toString(hasFollowRead));
        cache.touch(key ,expirationInSeconds());
        if (future != null){
            try {
                return future.get();
            } catch (Exception e) {
                return 0L;
            }
        }
        return 0L;
    }

    public PicListenReportDayResult getReportDayResult(Long studentId, String dayRange){
        String key = generateKey(SCORE_RESULT_KEY_PREFIX, studentId, dayRange, "");
        RedisHashCommands<String, Object> redisHashCommands = redisCommands.sync().getRedisHashCommands();
        Map<String, Object> objectMap = redisHashCommands.hgetall(key);
        if (objectMap == null)
            return null;
        else
            return convert2PicListenReportDayResult(objectMap);
    }

    private PicListenReportDayResult convert2PicListenReportDayResult(Map<String, Object> objectMap) {
        if (objectMap == null)
            return null;
        PicListenReportDayResult dayResult = new PicListenReportDayResult();
        dayResult.setLearnTime(SafeConverter.toLong(objectMap.get(LEARN_TIME)));
        dayResult.setFollowReadSentenceCount(SafeConverter.toLong(objectMap.get(FOLLOW_READ_SENTENCE_COUNT)));
        dayResult.setPlaySentenceCount(SafeConverter.toLong(objectMap.get(PICLISTEN_SENTENCE_COUNT)));
        dayResult.setReportScore(SafeConverter.toLong(objectMap.get(REPORTE_SCORE), -1));
        dayResult.setHasFollowRead(SafeConverter.toBoolean(objectMap.get(HAS_FOLLOW_READ)));
        return dayResult;
    }


    public void addOneDayNewScore(Long studentId, String dayRange, Long score){
        String key = generateSevenDayScoreListKey(studentId);
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        hashAsyncCommands.hset(key, dayRange, SafeConverter.toString(score));
        cache.touch(key ,86400 * 7);
    }

    public void deleteSomeDayScore(Long studentId, List<DayRange> dayRangeList){
        String key = generateSevenDayScoreListKey(studentId);
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        for (DayRange range : dayRangeList) {
            hashAsyncCommands.hdel(key, range.toString());
        }
    }

    public List<PicListenReportDayResult.DayScoreMapper> getSevenDayScoreList(Long studentId){
        String key = generateSevenDayScoreListKey(studentId);
        RedisHashCommands<String, Object> redisHashCommands = redisCommands.sync().getRedisHashCommands();
        Map<String, Object> objectMap = redisHashCommands.hgetall(key);
        if (objectMap == null)
            return Collections.emptyList();
        List<PicListenReportDayResult.DayScoreMapper> dayScoreMapperList = new ArrayList<>();
        objectMap.entrySet().forEach( t -> {
            PicListenReportDayResult.DayScoreMapper scoreMapper = new PicListenReportDayResult.DayScoreMapper();
            scoreMapper.setDay(DayRange.parse(t.getKey()));
            scoreMapper.setScore(SafeConverter.toLong(t.getValue()));
            dayScoreMapperList.add(scoreMapper);
        });
        return dayScoreMapperList;
    }



    private String generateSevenDayScoreListKey(Long studentId){
        return cacheKey("sevenDay-" + studentId);
    }

    private String generateDayActiveStudentIdSetKey(DayRange dayRange){
        Objects.requireNonNull(dayRange);
        return cacheKey(DAY_ACTIVE_STUDENT_ID_PREFIX + "-" + dayRange.toString());
    }

    public static void main(String[] args) {
        System.out.println(CacheKeyGenerator.generateCacheKey(PicListenReportCacheManager.class, "sevenDay-333888789"));
    }

    private String generateKey(String prefix, Long studentId, String dayRange, String type){
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(studentId);
        Objects.requireNonNull(dayRange);
        return cacheKey(prefix + "-"+ type + "-" + studentId + "-" +dayRange);
    }

}
