package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.report.WeekPushTeacher;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.concurrent.TimeUnit;


@ServiceVersion(version = "20170710")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface WeekReportLoader extends IPingable {
    //周报告列表
    @Idempotent
    MapMessage fetchWeekReportBrief(Teacher teacher);


    @Idempotent
    MapMessage fetchWeekReportBriefV2(Teacher teacher);

    @Idempotent
    MapMessage fetchWeekReportBrief(Long studentId);

    //周报告班级信息
    @Idempotent
    MapMessage fetchWeekReportForClazz(String groupIdAndReportId, User user);

    //获取班级信息
    @Idempotent
    MapMessage fetchWeekClazzInfo(List<String> groupIdAndReportIds, String teacherIdReportEndTime);

    @Idempotent
    Page<WeekPushTeacher> loadWeekPushTeacherByPage(Pageable pageable);


    //周报告个人信息
    @Idempotent
    MapMessage fetchWeekReportForStudent(Subject subject, String studentReportId, User user);
}
