package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.BasicReviewHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.baiscreview.report.crm.PackageHomeworkDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180612")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface BasicReviewHomeworkReportLoader extends IPingable {

    //班级关卡信息 app
    @Idempotent
    MapMessage fetchStageListToClazz(String packageId);

    //老师有作业包的班级列表 h5 and app
    @Idempotent
    MapMessage fetchBasicReviewClazzInfo(Teacher teacher, boolean fromPc);

    //班级一份作业信息 h5
    @Idempotent
    MapMessage fetchReportToClazz(String packageId, String homeworkId);

    //个人关卡信息 h5
    @Idempotent
    MapMessage fetchStageListToPersonal(String packageId, Long userId);

    //个人一个卡片作业的报告 h5
    @Idempotent
    MapMessage fetchReportToPersonal(String packageId, String homeworkId, Long userId,User parent);

    //个人有基础的学科 h5
    @Idempotent
    MapMessage fetchSubjectsToPersonal(Long userId);

    //老师发送分享消息
    @Idempotent
    MapMessage pushBasicReviewReportMsgToJzt(Teacher teacher, String packageId, String homeworkId);

    @Idempotent
    Map<Subject,PackageHomeworkDetail> crmPackageHomeworkDetail(Long userId);

    //期末复习作业历史
    @Idempotent
    List<BasicReviewHomeworkHistory> basicReviewHomeworkHistory(Teacher teacher);
}
