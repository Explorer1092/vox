package com.voxlearning.utopia.service.campaign.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningCount;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningWeekInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190118")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface TeacherWinterPlanService {

    MapMessage loadTeacherStatus(Long teacherId);

    MapMessage loadTeacherClazzInfo(Long teacherId);

    MapMessage loadStudentPlanDetail(Long studentId);

    MapMessage assgin(Long id);

    List<Long> loadStudentTeacherId(Long studentId);

    List<StudentPlanningCount> loadStudentPlanningCountInfo(Long teacherId);

    List<StudentPlanningWeekInfo> loadStudentPlanningWeekInfo(Long studentId);

    Boolean loadTeacherAssignStatus(Long studentId);

    MapMessage studentAssgin(Long studentId);

    void sendSmsPush(Long teacherId);

    MapMessage delTeacherActivityRef(Long teacherId, String activityType);
}
