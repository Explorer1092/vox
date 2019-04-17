package com.voxlearning.utopia.service.reward.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.PublicGoodLoader;
import com.voxlearning.utopia.service.reward.api.mapper.CommentEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.LikeEntriesMapper;
import com.voxlearning.utopia.service.reward.api.mapper.TeacherJoinStatusMapper;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.PGParentChildRef;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PublicGoodLoaderClient {

    @ImportService(interfaceClass = PublicGoodLoader.class)
    private PublicGoodLoader remoteReference;

    public List<PublicGoodStyle> loadStyleByModel(String model){
        return remoteReference.loadStyleByModel(model);
    }

    public List<PublicGoodStyle> loadStyleAll() {
        return remoteReference.loadStyleAll();
    }

    public PublicGoodStyle loadStyleById(Long id) {
        return loadStyleAll()
                .stream()
                .filter(s -> Objects.equals(s.getId(), id))
                .findFirst()
                .orElse(null);
    }

    public List<PublicGoodCollect> loadCollectByUserId(Long userId){
        return remoteReference.loadCollectByUserId(userId);
    }

    public PublicGoodCollect loadUserCollectById(Long userId,String collectId){
        return loadCollectByUserId(userId)
                .stream()
                .filter(c -> Objects.equals(c.getId(),collectId))
                .findFirst()
                .orElse(null);
    }

    public List<PublicGoodCollect> loadUserCollectByActId(Long userId,Long activityId){
        return remoteReference.loadUserCollectByActId(userId,activityId);
    }

    public List<PublicGoodElementType> loadAllElementType(){
        return remoteReference.loadAllElementTypes();
    }

    public List<PublicGoodElementType> loadElementTypeByStyleId(Long styleId){
        return  remoteReference.loadAllElementTypes()
                .stream()
                .filter(et -> Objects.equals(et.getStyleId(),styleId))
                .collect(Collectors.toList());
    }

    public PublicGoodElementType loadElementTypeByCode(String code){
        return remoteReference.loadAllElementTypes()
                .stream()
                .filter(et -> Objects.equals(et.getCode(), code)).findFirst().orElse(null);
    }

    public List<PublicGoodFeed> loadFeedByUserIdCollectId(Long activityId, Long userId, Boolean isRead) {
        return remoteReference.loadFeedByUserId(activityId, userId,isRead);
    }

    public Boolean getFeedStatus(Long activityId, Long userId) {
        return remoteReference.getFeedStatus(activityId, userId);
    }

    public MapMessage loadClazzCollect(Long activityId, Long userId) {
        return remoteReference.loadClazzCollect(activityId, userId);
    }

    public MapMessage loadSchoolRank(Long userId,Long activityId){
        return remoteReference.loadSchoolRank(userId,activityId);
    }

    public MapMessage loadNationRank(Long userId,Long activityId){
        return remoteReference.loadNationRank(userId,activityId);
    }

    public Set<Long> loadLikedCollect(Long activityId, Long userId) {
        return remoteReference.loadLikedCollect(activityId, userId);
    }

    public List<PublicGoodUserActivity> loadUserActivityByUserId(Long userId) {
        return remoteReference.loadUserActivityByUserId(userId);
    }

    public PublicGoodUserActivity loadUserActivityByUserId(Long activityId, Long userId) {
        return remoteReference.loadUserActivityByUserId(activityId, userId);
    }

    public List<LikeEntriesMapper> getLikeByCollectId(Long activityId, String collectId) {
        return remoteReference.getLikeByCollectId(activityId, collectId);
    }

    public List<TeacherJoinStatusMapper> getTeacherJoinStatus(Long activityId, Long userId) {
        return remoteReference.getTeacherJoinStatus(activityId, userId);
    }

    public CommentEntriesMapper getCommentByCollectId(Long activityId, String collectId) {
        return remoteReference.getCommentByCollectId(activityId, collectId);
    }

    public PGParentChildRef loadParentChildRef(Long parentId) {
        return remoteReference.loadParentChildRef(parentId);
    }

    public List<PublicGoodReward> loadRewardByModel(String model) {
        return remoteReference.loadRewardByModel(model);
    }

    public MapMessage loadRankForBackDoor(Long activityId,Long schoolId) {
        return remoteReference.loadRankForBackDoor(activityId,schoolId);
    }
}
