package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveCastHomeworkDetail;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkBriefPage;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.LiveHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.livecast.StudentLiveHomeworkDetail;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20170313")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface DPLiveCastHomeworkReportLoader extends IPingable {

    //一份作业的报告
    @Idempotent
    LiveHomeworkReport obtainLiveHomeworkReport(String hid);

    //21crm
    @Idempotent
    Map<String, LiveCastHomework> obtainLiveHomeworkMap(List<String> hids);



    //作业列表
    @Idempotent
    LiveHomeworkBriefPage pageHomeworkReportListByGroupIds(Collection<Long> groupIds, Integer page, Integer size);

    @Idempotent
    StudentLiveHomeworkDetail fetchStudentLiveHomeworkDetail(String hid, Long userId);

    @Idempotent
    Map<String, LiveCastHomeworkDetail> fetchLiveCastHomeworkDetail(Collection<String> homeworkIds);

    //一份作业详情
    @Idempotent
    MapMessage loadLiveCastHomeworkReportDetail(String homeworkId);

    //一份作业个人详情
    @Idempotent
    MapMessage loadLiveCastHomeworkReportDetail(String homeworkId, Long studentId);


    //基础练习 一个category班级详情
    @Idempotent
    MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, ObjectiveConfigType objectiveConfigType);

    //基础练习 一个category个人详情
    @Idempotent
    MapMessage reportDetailsBaseApp(String homeworkId, String categoryId, String lessonId, Long studentId, ObjectiveConfigType objectiveConfigType);


    //一本绘本个人详情
    @Idempotent
    MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId);

    @Idempotent
    MapMessage personalReadingDetail(String homeworkId, Long studentId, String readingId,  ObjectiveConfigType type);


    @Idempotent
    MapMessage fetchSubjectiveQuestion(String hid, ObjectiveConfigType objectiveConfigType, String qid);

    @Idempotent
    Boolean judgeLiveHomeworkSubjective(String hid);

    @Idempotent
    Boolean judgeLiveHomeworkObjective(String hid);

    @Idempotent
    MapMessage loadExamQuestionByIds(String data);

    @Idempotent
    Integer judgeLiveHomeworkType(String hid);


    @Idempotent
    MapMessage personalDubbingDetail(String homeworkId, Long studentId, String dubbingId, Long teacherId);

    @Idempotent
    MapMessage loadHomeworkQuestionsAnswer(String objectiveConfigType, String homeworkId, Long studentId, Integer categoryId, String lessonId, String videoId);
}
