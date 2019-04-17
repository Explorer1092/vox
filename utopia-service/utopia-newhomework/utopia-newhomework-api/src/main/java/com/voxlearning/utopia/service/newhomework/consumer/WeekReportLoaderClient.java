package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.WeekReportLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.report.WeekPushTeacher;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;

public class WeekReportLoaderClient implements WeekReportLoader {

    @ImportService(interfaceClass = WeekReportLoader.class)
    private WeekReportLoader hydraRemoteReference;

    @Override
    public MapMessage fetchWeekReportBrief(Teacher teacher) {
        return hydraRemoteReference.fetchWeekReportBrief(teacher);
    }

    @Override
    public MapMessage fetchWeekReportBriefV2(Teacher teacher) {
        return hydraRemoteReference.fetchWeekReportBriefV2(teacher);
    }

    @Override
    public MapMessage fetchWeekReportBrief(Long studentId) {
        return hydraRemoteReference.fetchWeekReportBrief(studentId);
    }

    @Override
    public MapMessage fetchWeekReportForClazz(String groupIdAndReportId, User user) {
        return hydraRemoteReference.fetchWeekReportForClazz(groupIdAndReportId, user);
    }

    @Override
    public MapMessage fetchWeekClazzInfo(List<String> groupIdAndReportIds, String teacherIdReportEndTime) {
        return hydraRemoteReference.fetchWeekClazzInfo(groupIdAndReportIds, teacherIdReportEndTime);
    }

    @Override
    public Page<WeekPushTeacher> loadWeekPushTeacherByPage(Pageable pageable) {
        return hydraRemoteReference.loadWeekPushTeacherByPage(pageable);
    }

    @Override
    public MapMessage fetchWeekReportForStudent(Subject subject, String studentReportId, User user) {
        return hydraRemoteReference.fetchWeekReportForStudent(subject, studentReportId, user);
    }
}
