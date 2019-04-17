package com.voxlearning.utopia.service.newhomework.api;


import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.outside.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190304")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
public interface OutsideReadingLoader extends IPingable {

    /**
     * 查询我的书架
     *
     * @param userId 用户ID
     * @return {@link StudentBookshelfResp}
     */
    MapMessage loadBookshelf(Long userId, String cdnUrl);

    /**
     * 查询当前学生课外阅读任务完成状态
     * @param userId 学生ID
     * @return <outsideReadingId, 是否完成>
     */
    Map<String, Boolean> loadOutsideReadingStatus(Long userId);

    /**
     * 查询当前学生未完成课外阅读列表
     */
    List<OutsideReading> loadUnFinishedOutsideReadings(Long userId);

    /**
     * 查询学生读书详情
     *
     * @param userId 学生ID
     * @param outsideReadingId 课外阅读任务ID
     * @return {@link StudentBookDetailResp}
     */
    MapMessage loadStudentBookDetail(Long userId, String outsideReadingId);

    /**
     * 根据任务id取阅读任务信息
     * @param outsideReadingId
     * @return
     */
    OutsideReading findOutsideReadingById(String outsideReadingId);

    /**
     * 根据阅读任务id和关卡id获取题
     * @param outsideReadingId
     * @param missionId
     * @return
     */
    MapMessage loadQuestions(String outsideReadingId, String missionId);

    /**
     * 根据阅读任务id和关卡id获取用户答案
     * @param studentId
     * @param outsideReadingId
     * @param missionId
     * @return
     */
    MapMessage loadQuestionsAnswer(Long studentId, String outsideReadingId, String missionId);

    /**
     * 好词好句收藏页
     * @param userId
     * @param outsideReadingId
     * @param missionId
     * @return
     */
    MapMessage goldenWordsIndex(Long userId, String outsideReadingId, String missionId);

    /**
     * 关卡阅读成就奖励
     * @param userId
     * @param outsideReadingId
     * @param missionId
     * @return
     */
    MapMessage fetchMissionAchievement(Long userId, String outsideReadingId, String missionId);

    /**
     * 获取学生总阅读成就
     * @param userId
     * @return {@link ReadingAchievementResp}
     */
    MapMessage fetchAchievement(Long userId, String cdnUrl);

    /**
     * 查询老师图书报告列表
     * @param teacherId
     * @param groupId
     * @return {@link TeacherReportBookListResp}
     */
    MapMessage loadReportBookList(Long teacherId, Long groupId);

    /**
     * 查询老师图书报告详情
     * @param readingId
     * @return {@link TeacherReportBookDetailResp}
     */
    MapMessage loadReportBookDetail(String readingId);

    /**
     * 查看学生主观题作答详情
     * @param readingId 阅读任务ID
     * @return
     */
    MapMessage fetchAnswerDetail(String readingId, String cdnUrl);

    /**
     * 查询学生主观题昨天详情分享接口
     * @param readingId 阅读任务ID
     * @param processResultId 做题详情ID
     * @return
     */
    MapMessage fetchAnswerShareDetail(String readingId, String processResultId);

    /**
     * 查询班级成就
     * @param groupId
     * @return {@link ClazzAchievementResp}
     */
    MapMessage loadReportClazzAchievement(Long groupId);

    /**
     * 根据班组ID查询阅读任务
     * @param groupIds
     * @return
     */
    Map<Long, List<OutsideReading>> loadOutsideReadingByGroupId(Collection<Long> groupIds);

    /**
     * 好词好句列表
     * @param userId 学生ID
     * @param label 标签名
     * @param pageNum
     * @param pageSize
     * @return
     */
    MapMessage fetchGoldenWordsList(Long userId, String label, Integer pageNum, Integer pageSize);

    MapMessage crmLoadOutsideReadingsByGroupId(Long groupId);
}
