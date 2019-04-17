package com.voxlearning.utopia.service.vendor.impl.dao;

import com.lambdaworks.redis.api.async.RedisHashAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisHashCommands;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.StudyAppData;

import javax.inject.Named;
import java.util.Map;

/**
 * 我的学习数据
 *
 * @author jiangpeng
 * @since 2017-05-12 上午11:12
 **/
@Named
public class StudyAppDataDao extends SpringContainerSupport {

    private IRedisCommands redisCommands;

    private static String KEY_PREFIX = "studyAppData_";


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        redisCommands = instance.getRedisCommands("parent-app");
    }


    public StudyAppData loadUserStudyAppData(Long userId, SelfStudyType selfStudyType){
        if (userId == null || userId == 0 || selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return null;
        String key = generateKey(userId, selfStudyType);
        RedisHashCommands<String, Object> redisHashCommands = redisCommands.sync().getRedisHashCommands();
        Map<String, Object> mapFuture = redisHashCommands.hgetall(key);
        return StudyAppData.fromMap(mapFuture, userId, selfStudyType);
    }

    public void updateProgress(Long userId, SelfStudyType selfStudyType, String progress){
        if (userId == null || userId == 0 || selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return;
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        String key = generateKey(userId, selfStudyType);
        hashAsyncCommands.hset(key, StudyAppData.PROGRESS, progress == null ? "" : progress);
    }

    public void updateIcon(Long userId, SelfStudyType selfStudyType, String iconUrl){
        if (userId == null || userId == 0 || selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return;
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        String key = generateKey(userId, selfStudyType);
        hashAsyncCommands.hset(key, StudyAppData.ICONURL, iconUrl);
    }

    public void updateShow(Long userId, SelfStudyType selfStudyType, Boolean booked){
        if (userId == null || userId == 0 || selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN
                || booked == null)
            return;
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        String key = generateKey(userId, selfStudyType);
        hashAsyncCommands.hset(key, StudyAppData.BOOKING, booked.toString());
    }

    public void updateNotify(Long userId, SelfStudyType selfStudyType, String notifyContent, String notifyUniqueId){
        if (userId == null || userId == 0 || selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN
                || StringUtils.isBlank(notifyContent) || StringUtils.isBlank(notifyUniqueId))
            return;
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        String key = generateKey(userId, selfStudyType);
        hashAsyncCommands.hset(key, StudyAppData.NOTIFYCONTENT, notifyContent);
        hashAsyncCommands.hset(key, StudyAppData.NOTIFYUNIQUEID, notifyUniqueId);
    }

    public void updateAlbumCount(Long userId, SelfStudyType selfStudyType, Long delta){
        if (userId == null || userId == 0 || selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN
                || delta == null || delta == null)
            return;
        RedisHashAsyncCommands<String, Object> hashAsyncCommands = redisCommands.async().getRedisHashAsyncCommands();
        String key = generateKey(userId, selfStudyType);
        hashAsyncCommands.hincrby(key, StudyAppData.ALBUMCOUNT, delta);
    }


    private String generateKey(Long userId, SelfStudyType selfStudyType) {
        return KEY_PREFIX + userId + "_" + selfStudyType.name();
    }

}
