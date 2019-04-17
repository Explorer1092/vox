package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.BasicReviewHomeworkReportLoader;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.crm.PackageHomeworkDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;

public class BasicReviewHomeworkReportLoaderClient implements BasicReviewHomeworkReportLoader {
    @ImportService(interfaceClass = BasicReviewHomeworkReportLoader.class)
    private BasicReviewHomeworkReportLoader remoteReference;

    @Override
    public MapMessage fetchStageListToClazz(String packageId) {
        return remoteReference.fetchStageListToClazz(packageId);
    }

    @Override
    public MapMessage fetchBasicReviewClazzInfo(Teacher teacher, boolean fromPc) {
        return remoteReference.fetchBasicReviewClazzInfo(teacher, fromPc);
    }

    @Override
    public MapMessage fetchReportToClazz(String packageId, String homeworkId) {
        return remoteReference.fetchReportToClazz(packageId, homeworkId);
    }

    @Override
    public MapMessage fetchStageListToPersonal(String packageId, Long userId) {
        return remoteReference.fetchStageListToPersonal(packageId, userId);
    }

    @Override
    public MapMessage fetchReportToPersonal(String packageId, String homeworkId, Long userId,User parent) {
        return remoteReference.fetchReportToPersonal(packageId, homeworkId, userId,parent);
    }

    @Override
    public MapMessage fetchSubjectsToPersonal(Long userId) {
        return remoteReference.fetchSubjectsToPersonal(userId);
    }

    @Override
    public MapMessage pushBasicReviewReportMsgToJzt(Teacher teacher, String packageId, String homeworkId) {
        return remoteReference.pushBasicReviewReportMsgToJzt(teacher, packageId, homeworkId);
    }

    @Override
    public Map<Subject, PackageHomeworkDetail> crmPackageHomeworkDetail(Long userId) {
        return remoteReference.crmPackageHomeworkDetail(userId);
    }

    @Override
    public List<BasicReviewHomeworkHistory> basicReviewHomeworkHistory(Teacher teacher) {
        return remoteReference.basicReviewHomeworkHistory(teacher);
    }
}
