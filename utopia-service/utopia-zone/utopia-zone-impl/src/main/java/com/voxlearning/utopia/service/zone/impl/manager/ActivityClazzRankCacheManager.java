package com.voxlearning.utopia.service.zone.impl.manager;

import com.lambdaworks.redis.Range;
import com.lambdaworks.redis.ScoredValue;
import com.lambdaworks.redis.api.sync.RedisKeyCommands;
import com.lambdaworks.redis.api.sync.RedisSortedSetCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.vo.ActivityRank;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author chensn
 * @date 2018-11-10 14:15
 */
@Named
public class ActivityClazzRankCacheManager extends SpringContainerSupport {

    private static final String LEVEL_RANK_PREFIX = "CLAZZ_ZONE_ACVITITY_RANK:";
    private static final Long EXPIRE_TIME = 60 * 24 * 60 * 16L;     // 设置为16天

    private IRedisCommands redisCommands;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder commandsBuilder = RedisCommandsBuilder.getInstance();
        redisCommands = commandsBuilder.getRedisCommands("picbook-redis");
    }

    /**
     * type 1、或者 4 为个人 传用户id  type 为2或者5,传classId
     *  1.个人班级榜（每日）2 个人年级榜 4个人全校榜 3.班级年级榜 5班级全校榜
     * @param activityId
     * @param schoolId
     * @param id
     * @param level type 为 1 ，2时必传
     * @param num        增加数量
     * @param type
     * @return
     */
    public Long updateRank(Integer activityId, Long schoolId, Long id, Integer level, Long clazzId, Integer num, Integer type, Integer timeType) {
        String key;
        String timeKey = null;
        if (timeType != null && timeType == 1) {
            //每天缓存一个榜
            timeKey = DateFormatUtils.format(new Date(), "yyyyMMdd");
        }
        if (type == 2 || type == 3) {
            key = getKey(activityId, schoolId, level, type, timeKey);
        } else if (type == 1) {
            key = getPersonClazzKey(activityId, schoolId, clazzId, timeKey);
        } else {
            key = getSchoolKey(activityId, schoolId, type, timeKey);
        }
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        RedisKeyCommands<String, Object> keyCommands = redisCommands.sync().getRedisKeyCommands();
        sortedSetCommands.zaddincr(key, num, id);
        if (keyCommands.ttl(key) == -1) {
            keyCommands.expire(key, EXPIRE_TIME);
        }
        return sortedSetCommands.zrevrank(key, id) + 1;
    }


    /**
     * @param limit
     * @param activityId
     * @param schoolId
     * @param level
     * @param type       1.个人班级榜 2 个人年级榜 4个人全校榜 3.班级年级榜 5班级全校榜
     * @return
     */
    public List<ActivityRank> getRank(Integer limit, Integer activityId, Long schoolId, Integer level, Long clazzId, Integer type, Integer timeType, Date date) {

        String key;
        String timeKey = null;
        if (timeType != null && timeType == 1) {
            //每天缓存一个榜
            if (date == null) {
                date = new Date();
            }
            timeKey = DateFormatUtils.format(date, "yyyyMMdd");
        }
        if (type == 2 || type == 3) {
            key = getKey(activityId, schoolId, level, type, timeKey);
        } else if (type == 1) {
            key = getPersonClazzKey(activityId, schoolId, clazzId, timeKey);
        } else {
            key = getSchoolKey(activityId, schoolId, type, timeKey);
        }
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
        List<ActivityRank> list = new ArrayList<>();
        for (ScoredValue<Object> scoredValue : scoredValues) {
            // 加入实际排名
            ActivityRank ar = new ActivityRank();
            if (type == 2 || type == 4 || type == 1) {
                ar.setUserId(SafeConverter.toLong(scoredValue.value));
            } else {
                ar.setClazzId(SafeConverter.toLong(scoredValue.value));
            }
            ar.setNum(new Double(scoredValue.score).intValue());
            list.add(ar);
        }
        return list;
    }

    /**
     * type 1为个人 传用户id  type 为2,传classId
     *
     * @param activityId
     * @param schoolId
     * @param level
     * @param id
     * @param type 1 个人年级榜 4个人全校榜 2.班级年级榜 5班级全校榜
     * @return
     */
    public ActivityRank getSelfRank(Integer activityId, Long schoolId, Integer level, Long clazzId, Long id, Integer type, Integer timeType, Date date) {
        if (type == null) {
            return null;
        }
        String key;
        String timeKey = null;
        if (timeType != null && timeType == 1) {
            //每天缓存一个榜
            if (date == null) {
                date = new Date();
            }
            timeKey = DateFormatUtils.format(date, "yyyyMMdd");
        }
        if (type == 2 || type == 3) {
            key = getKey(activityId, schoolId, level, type, timeKey);
        } else if (type == 1) {
            key = getPersonClazzKey(activityId, schoolId, clazzId, timeKey);
        } else {
            key = getSchoolKey(activityId, schoolId, type, timeKey);
        }
        RedisSortedSetCommands<String, Object> sortedSetCommands = redisCommands.sync().getRedisSortedSetCommands();
        Double zscore = sortedSetCommands.zscore(key, id);
        Long zrank = sortedSetCommands.zrevrank(key, id);
        // 加入实际排名
        ActivityRank ar = new ActivityRank();
        if (zrank != null){
            ar.setIndex(SafeConverter.toInt(zrank+1));
        }
        if (zscore != null) {
            ar.setNum(zscore.intValue());
        }
        if (type == 2 || type == 4 || type == 1) {
            ar.setUserId(id);
        } else {
            ar.setClazzId(id);
        }
        return ar;
    }


    private String getKey(Integer activityId, Long schoolId, Integer level, Integer type, String timeKey) {
        if (StringUtils.isBlank(timeKey)) {
            return LEVEL_RANK_PREFIX + activityId + "_" + schoolId + "_" + level + "_" + type;
        } else {
            return LEVEL_RANK_PREFIX + activityId + "_" + schoolId + "_" + level + "_" + type + "_" + timeKey;
        }
    }

    private String getSchoolKey(Integer activityId, Long schoolId, Integer type, String timeKey) {
        if (StringUtils.isBlank(timeKey)) {
            return LEVEL_RANK_PREFIX + activityId + "_" + schoolId + "_" + type;
        } else {
            return LEVEL_RANK_PREFIX + activityId + "_" + schoolId + "_" + type + "_" + timeKey;
        }
    }

    private String getPersonClazzKey(Integer activityId, Long schoolId, Long clazzId, String timeKey) {
        return LEVEL_RANK_PREFIX + activityId + "_" + schoolId + "_clazz:" + clazzId + "_" + timeKey;
    }

}
