package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190320")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface WarmHeartPlanService {

    MapMessage loadTeacherStatus(User user);

    MapMessage assgin(Long teacherId);

    MapMessage loadTeacherClazzInfo(Long teacherId);

    /**
     * 老师参与统计
     */
    Long incrParticipateCount(Long incr);

    Long getParticipateCount();

    /**
     * 学生参与统计
     */
    Long incrStuParticipateCount(Long incr);

    Long getStuParticipateCount();

    /**
     * 查看学生暖心计划状态
     * @param id
     * @param studentId
     * @return
     */
    MapMessage loadStudentStatus(Long id, Long studentId);

    /**
     * 展示暖心计划所有目标详情
     * @return
     * @param id
     * @param imgUrl
     * @param studentId
     */
    MapMessage warmHeartTargets(Long id, String imgUrl, Long studentId);

    MapMessage loadStudentStatus(User user);

    /**
     * 保存暖心计划目标
     * @param studentId
     * @param plans
     */
    MapMessage saveWarmHeartPlans(long studentId, String plans);

    /**
     * 查看单个学生参与详情
     * @param tid
     * @param classId
     * @return
     */
    MapMessage loadClockInfo(Long tid, long classId);

    /**
     * 查看单个学生打卡详情
     * @param studentId
     * @return
     */
    MapMessage loadStudentClockInfo(long studentId);

    /**
     * 教师点赞学生
     * @param studentId
     * @return
     */
    MapMessage praise(long studentId);

    Boolean loadTeacherAssignStatus(Long studentId);

    MapMessage backdoorDelKey(String key);

    MapMessage backdoorIncrKey(String key, Long incr, Long init);
}
