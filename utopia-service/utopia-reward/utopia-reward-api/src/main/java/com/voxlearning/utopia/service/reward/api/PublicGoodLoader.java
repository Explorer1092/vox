package com.voxlearning.utopia.service.reward.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.mapper.CommentEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.LikeEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.TeacherJoinStatusMapper;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.PGParentChildRef;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ServiceVersion(version = "20180612")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface PublicGoodLoader {

    int RANK_LIMIT = 100;

    PublicGoodStyle loadStyleById(Long id);

    List<PublicGoodStyle> loadStyleByModel(String model);

    List<PublicGoodStyle> loadStyleAll();

    List<PublicGoodCollect> loadCollectByUserId(Long userId);

    default List<PublicGoodCollect> loadUserCollectByActId(Long userId,Long activityId){
        return loadCollectByUserId(userId)
                .stream()
                .filter(collect -> Objects.equals(collect.getActivityId(),activityId))
                .collect(Collectors.toList());
    }

    default PublicGoodCollect loadCollectById(Long userId,String collectId){
        return loadCollectByUserId(userId)
                .stream()
                .filter(c -> Objects.equals(collectId,c.getId()))
                .findFirst()
                .orElse(null);
    }

    List<PublicGoodElementType> loadAllElementTypes();

    default PublicGoodElementType loadElementTypeByCode(String code){
        return loadAllElementTypes()
                .stream()
                .filter(et -> Objects.equals(et.getCode(),code))
                .findFirst()
                .orElse(null);
    }

    List<PublicGoodElementType> loadElementTypeByStyleId(Long styleId);

    List<PublicGoodFeed> loadFeedByUserId(Long activityId, Long userId, Boolean isRead);

    Boolean getFeedStatus(Long activityId, Long userId);

    List<PublicGoodUserActivity> loadUserActivityByUserId(Long userId);

    default PublicGoodUserActivity loadUserActivityByUserId(Long activityId, Long userId) {
        return loadUserActivityByUserId(userId)
                .stream()
                .filter(i -> Objects.equals(i.getActivityId(), activityId))
                .findFirst()
                .orElse(null);
    }

    MapMessage loadClazzCollect(Long activityId, Long userId);

    MapMessage loadSchoolRank(Long userId,Long activityId);

    MapMessage loadNationRank(Long userId,Long activityId);

    List<LikeEntriesMapper> getLikeByCollectId(Long activityId, String collectId);

    CommentEntriesMapper getCommentByCollectId(Long activityId, String collectId);

    List<TeacherJoinStatusMapper> getTeacherJoinStatus(Long activityId, Long userId);

    Set<Long> loadLikedCollect(Long activityId, Long userId);

    List<PublicGoodReward> loadRewardByModel(String model);

    PGParentChildRef loadParentChildRef(Long parentId);

    MapMessage loadRankForBackDoor(Long activityId,Long schoolId);
}
