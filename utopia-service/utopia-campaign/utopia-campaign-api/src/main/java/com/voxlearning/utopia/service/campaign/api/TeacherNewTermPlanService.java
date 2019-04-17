package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190311")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherNewTermPlanService {

    MapMessage loadTeacherStatus(Long teacherId);

    MapMessage loadTeacherClazzInfo(Long teacherId);

    MapMessage assgin(Long id);

    MapMessage loadClockInfo(Long teacherId, Long classId);

    MapMessage loadStudentClockInfo(Long studentId);

    MapMessage praise(Long studentId);

    MapMessage sendParentPush(Long id);

    MapMessage getTargetDetail(Long id);

    Boolean loadTeacherAssignStatus(Long studentId);

    /**
     * 老师参与统计
     */
    Long incrParticipateCount(Long incr);

    Long setParticipateCount(Long incr);

    Long getParticipateCount();

    /**
     * 新学期计划第二轮老师报名统计
     */
    MapMessage teacherSignUp(Long id);

    Long getTeacherSignUpCount();

    Long incrTeacherSignUpCount(Long incr);

    Long setTeacherSignUpCount(Long count);

    Boolean getTeacherSignUpStatus(Long studentId);

    Set<Long> getAllStudentIdByTeacherId(Long teacherId);
}
