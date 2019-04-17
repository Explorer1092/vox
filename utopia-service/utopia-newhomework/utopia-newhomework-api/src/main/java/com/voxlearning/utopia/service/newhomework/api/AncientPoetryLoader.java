package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.ModelType;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryActivity;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.StudentActivityStatistic;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/20
 */
@ServiceVersion(version = "20190228")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
public interface AncientPoetryLoader {

    /**
     * 老师活动列表
     */
    MapMessage fetchPoetryActivityList(TeacherDetail teacher, Long clazzGroupId, Integer clazzLevel);

    /**
     * 班级活动列表
     */
    MapMessage fetchGroupActivityList(Long clazzGroupId, Boolean needDetail, Long studentId);

    /**
     * 班级活动列表
     * @param clazzGroupId 班组ID
     * @return
     */
    List<AncientPoetryActivity> loadActivityByGroupId(Long clazzGroupId);

    /**
     * 查询活动关卡列表
     */
    MapMessage fetchActivityMissions(User user, String activityId, Long groupId, Long studentId);

    /**
     * 查询关卡详情
     */
    MapMessage fetchMissionDetail(User user, String activityId, String missionId);

    /**
     * 查询关卡模块详情
     */
    MapMessage fetchMissionModelDetail(String activityId, String missionId, ModelType modelType);

    /**
     * 查询模块完成结果
     */
    MapMessage fetchMissionModelResult(String activityId, String missionId, Long studentId, ModelType modelType, Boolean isParentMission, String cdnUrl);

    /**
     * 查询关卡中间结果
     */
    MapMessage fetchMissionResult(String activityId, String missionId, Long studentId);

    /**
     * 班级排行榜
     */
    MapMessage clazzRankingList(User user, Long groupId, String activityId);

    /**
     * 总榜
     */
    MapMessage globalRankingList(Integer provinceId, Integer regionCode, Long schoolId, Integer clazzLevel, String regionLevel, String cdnUrl, Long studentId);

    /**
     * 通过ID查活动
     */
    AncientPoetryActivity findActivityById(String activityId);

    /**
     * 查询所有活动
     */
    List<AncientPoetryActivity> loadAllActivity();

    /**
     * 家长当前学生已参加活动列表
     */
    MapMessage parentChildActivityList(Long studentId, Long groupId);

    /**
     * 通过古诗ID查询古诗
     * @param poetryIds
     */
    Map<String, AncientPoetryMission> fetchAncientPoetryMissionByIds(List<String> poetryIds);

    /**
     * 查询所有古诗
     */
    List<AncientPoetryMission> loadAllPoetry();

    /**
     * 学生已参加活动汇总
     * 注: 家长通调用
     *
     * @param studentId 学生ID
     * @return {@link StudentActivityStatistic}
     */
    StudentActivityStatistic getStudentActivityStatistics(Long studentId);

    /**
     * 订正错题列表
     */
    MapMessage loadCorrectQuestions(String activityId, Long studentId);

    /**
     * 订正错题答案
     */
    MapMessage loadCorrectQuestionsAnswer(String activityId, Long studentId);

    /**
     * 查询学生总星级表中所有的SchoolId
     */
    List<Long> loadAllJoinedStudentsSchoolIds();

    /**
     * 查询学生总星级表中所有的RegionId
     */
    List<Integer> loadAllJoinedStudentsRegionIds();
}
