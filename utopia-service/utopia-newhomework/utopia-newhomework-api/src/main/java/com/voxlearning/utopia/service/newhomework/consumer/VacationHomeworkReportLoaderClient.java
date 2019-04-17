package com.voxlearning.utopia.service.newhomework.consumer;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewVacationHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.VacationReportForParent;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.VacationReportToSubject;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;

public class VacationHomeworkReportLoaderClient implements VacationHomeworkReportLoader {

    @ImportService(interfaceClass = VacationHomeworkReportLoader.class)
    private VacationHomeworkReportLoader remoteReference;

    @Override
    public List<VacationReportForParent> loadVacationReportForParent(Long studentId) {
        return remoteReference.loadVacationReportForParent(studentId);
    }

    @Override
    public List<VacationReportToSubject> loadVacationReportToSubject(Long studentId) {

        return remoteReference.loadVacationReportToSubject(studentId);
    }

    @Override
    public List<NewVacationHomeworkHistory> newVacationHomeworkHistory(Teacher teacher) {
        return remoteReference.newVacationHomeworkHistory(teacher);
    }

    @Override
    public List<NewVacationHomeworkHistory> allVacationHomeworkHistory(Teacher teacher) {
        return remoteReference.allVacationHomeworkHistory(teacher);
    }

    @Override
    public MapMessage vacationReportDetailInformation(String homeworkId) {
        return remoteReference.vacationReportDetailInformation(homeworkId);
    }

    @Override
    public MapMessage personalReadingDetail(String homeworkId, String readingId, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.personalReadingDetail(homeworkId, readingId, objectiveConfigType);
    }

    @Override
    public MapMessage personalReadReciteWithScore(String hid, String questionBoxId, Long sid) {
        return remoteReference.personalReadReciteWithScore(hid, questionBoxId, sid);
    }

    @Override
    public MapMessage personalDubbingDetail(String homeworkId,  String dubbingId) {
        return remoteReference.personalDubbingDetail(homeworkId,dubbingId);
    }

    @Override
    public MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType) {
        return remoteReference.reportDetailsBaseApp(homeworkId, categoryId, lessonId, objectiveConfigType);
    }

    @Override
    public MapMessage packageReport(String packageId, User user, Long sid, Boolean fromJzt) {
        return remoteReference.packageReport(packageId,user,sid, fromJzt);
    }

    @Override
    public Map<String, Object> studentVacationNewHomeworkDetail(String homeworkId) {
        return remoteReference.studentVacationNewHomeworkDetail(homeworkId);
    }

    @Override
    public MapMessage pushShareJztMsg(List<String> packageIds, Teacher teacher) {
        return remoteReference.pushShareJztMsg(packageIds, teacher);
    }

    @Override
    public MapMessage shareReportWeiXin(List<String> packageIds, Teacher teacher) {

        return remoteReference.shareReportWeiXin(packageIds,teacher);
    }

    @Override
    public MapMessage remindStudentMsg(String packageId, Teacher teacher) {
        return remoteReference.remindStudentMsg(packageId, teacher);
    }

    @Override
    public MapMessage studentPackageReport(String packageId, Long studentId) {
        return remoteReference.studentPackageReport(packageId, studentId);
    }

    @Override
    public MapMessage studentDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId) {
        return remoteReference.studentDubbingWithScoreDetail(homeworkId,studentId,dubbingId);
    }
}
