package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewVacationHomeworkHistory;
import com.voxlearning.utopia.service.newhomework.api.mapper.VacationReportForParent;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.report.VacationReportToSubject;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//假期作业报告接口
@ServiceVersion(version = "20190220")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface VacationHomeworkReportLoader extends IPingable {

    //家长通假期报告简介列表接口
    @Idempotent
    List<VacationReportForParent> loadVacationReportForParent(Long studentId);

    //家长通学生各个学科的假期包情况
    @Idempotent
    List<VacationReportToSubject> loadVacationReportToSubject(Long studentId);

    //作业list历史
    @Idempotent
    List<NewVacationHomeworkHistory> newVacationHomeworkHistory(Teacher teacher);

    /**
     * 假期作业list历史(包含被删除的)
     * 仅供CRM使用！！！
     */
    @Idempotent
    List<NewVacationHomeworkHistory> allVacationHomeworkHistory(Teacher teacher);

    //假期作业 一个包 整个班的信息
    @Idempotent
    MapMessage packageReport(String packageId, User user, Long sid, Boolean fromJzt);

    //学生整个假期作业报告
    @Idempotent
    MapMessage studentPackageReport(String packageId, Long studentId);

    //假期作业  一个学生 一个包 的报告
    @Idempotent
    MapMessage vacationReportDetailInformation(String homeworkId);

    //绘本信息
    @Idempotent
    MapMessage personalReadingDetail(String homeworkId, String readingId, ObjectiveConfigType objectiveConfigType);

    //趣味配音二级页面接口
    @Idempotent
    MapMessage personalDubbingDetail(String homeworkId, String dubbingId);

    //基础练习
    @Idempotent
    MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType);

    //假期作业一个学生一个包的作业答题信息 crm
    @Idempotent
    Map<String, Object> studentVacationNewHomeworkDetail(String homeworkId);

    //新朗读背诵单个app个人报告
    MapMessage personalReadReciteWithScore(String hid, String questionBoxId, Long sid);

    //发送家长端消息
    @Idempotent
    MapMessage pushShareJztMsg(List<String> packageIds, Teacher teacher);

    //分享微信QQ奖励抽奖
    @Idempotent
    MapMessage shareReportWeiXin(List<String> packageIds,Teacher teacher);

    @Idempotent
    MapMessage remindStudentMsg(String packageId, Teacher teacher);

    @Idempotent
    MapMessage studentDubbingWithScoreDetail(String homeworkId, Long studentId, String dubbingId);
}
