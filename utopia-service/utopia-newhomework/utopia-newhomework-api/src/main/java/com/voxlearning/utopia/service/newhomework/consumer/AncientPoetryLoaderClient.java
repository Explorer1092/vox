package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.AncientPoetryLoader;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.StudentActivityStatistic;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
public class AncientPoetryLoaderClient implements AncientPoetryLoader {

    @Getter
    @ImportService(interfaceClass = AncientPoetryLoader.class)
    private AncientPoetryLoader remoteReference;

    @Override
    public MapMessage fetchPoetryActivityList(TeacherDetail teacher, Long clazzGroupId, Integer clazzLevel) {
        return remoteReference.fetchPoetryActivityList(teacher, clazzGroupId, clazzLevel);
    }

    @Override
    public MapMessage fetchGroupActivityList(Long clazzGroupId, Boolean needDetail, Long studentId) {
        return remoteReference.fetchGroupActivityList(clazzGroupId, needDetail, studentId);
    }

    @Override
    public List<AncientPoetryActivity> loadActivityByGroupId(Long clazzGroupId) {
        return remoteReference.loadActivityByGroupId(clazzGroupId);
    }

    @Override
    public MapMessage fetchActivityMissions(User user, String activityId, Long groupId, Long studentId) {
        return remoteReference.fetchActivityMissions(user, activityId, groupId, studentId);
    }

    @Override
    public MapMessage fetchMissionDetail(User user, String activityId, String missionId) {
        return remoteReference.fetchMissionDetail(user, activityId, missionId);
    }

    @Override
    public MapMessage fetchMissionModelDetail(String activityId, String missionId, ModelType modelType) {
        return remoteReference.fetchMissionModelDetail(activityId, missionId, modelType);
    }

    @Override
    public MapMessage fetchMissionModelResult(String activityId, String missionId, Long studentId, ModelType modelType, Boolean isParentMission, String cdnUrl) {
        return remoteReference.fetchMissionModelResult(activityId, missionId, studentId, modelType, isParentMission, cdnUrl);
    }

    @Override
    public MapMessage fetchMissionResult(String activityId, String missionId, Long studentId) {
        return remoteReference.fetchMissionResult(activityId, missionId, studentId);
    }

    @Override
    public MapMessage clazzRankingList(User user, Long groupId, String activityId) {
        return remoteReference.clazzRankingList(user, groupId, activityId);
    }

    @Override
    public MapMessage globalRankingList(Integer provinceId, Integer regionCode, Long schoolId, Integer clazzLevel, String regionLevel, String cdnUrl, Long studentId) {
        return remoteReference.globalRankingList(provinceId, regionCode, schoolId, clazzLevel, regionLevel, cdnUrl, studentId);
    }

    @Override
    public AncientPoetryActivity findActivityById(String activityId) {
        return remoteReference.findActivityById(activityId);
    }

    @Override
    public List<AncientPoetryActivity> loadAllActivity() {
        return remoteReference.loadAllActivity();
    }

    @Override
    public MapMessage parentChildActivityList(Long studentId, Long groupId) {
        return remoteReference.parentChildActivityList(studentId, groupId);
    }

    @Override
    public Map<String, AncientPoetryMission> fetchAncientPoetryMissionByIds(List<String> poetryIds) {
        return remoteReference.fetchAncientPoetryMissionByIds(poetryIds);
    }

    @Override
    public List<AncientPoetryMission> loadAllPoetry() {
        return remoteReference.loadAllPoetry();
    }

    @Override
    public StudentActivityStatistic getStudentActivityStatistics(Long studentId) {
        return remoteReference.getStudentActivityStatistics(studentId);
    }

    @Override
    public MapMessage loadCorrectQuestions(String activityId, Long studentId) {
        return remoteReference.loadCorrectQuestions(activityId, studentId);
    }

    @Override
    public MapMessage loadCorrectQuestionsAnswer(String activityId, Long studentId) {
        return remoteReference.loadCorrectQuestionsAnswer(activityId, studentId);
    }

    @Override
    public List<Long> loadAllJoinedStudentsSchoolIds() {
        return remoteReference.loadAllJoinedStudentsSchoolIds();
    }

    @Override
    public List<Integer> loadAllJoinedStudentsRegionIds() {
        return remoteReference.loadAllJoinedStudentsRegionIds();
    }
}
