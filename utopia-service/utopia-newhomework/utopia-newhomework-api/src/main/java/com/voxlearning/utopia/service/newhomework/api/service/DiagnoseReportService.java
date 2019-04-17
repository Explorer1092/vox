package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Idempotent;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.ClazzGroupInfoResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.DiagnoseReportDetailResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.DiagnoseReportReportListResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.OralStudentQuestionResp;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.intelligentteaching.*;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190102")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface DiagnoseReportService extends IPingable {

    //查询老师班级信息
    @Idempotent
    List<ClazzGroupInfoResp> fetchClazzInfo(Collection<Long> teacherIds);

    //讲练测报告列表
    @Idempotent
    PageImpl<DiagnoseReportReportListResp> fetchReportList(Long groupId, Pageable pageable, Subject subject);

    //讲练测题包报告详情
    @Idempotent
    DiagnoseReportDetailResp fetchReportDetail(String hid, ObjectiveConfigType configType, String questionBoxId);

    //口语诊断查看题目详情
    @Idempotent
    List<OralStudentQuestionResp> oralStudentQuestionDetail(String hid, ObjectiveConfigType configType, String questionId);

    //统计学生作业即时干预纠正错题数
    @Idempotent
    long countInterventionGraspQuestion(NewHomework newHomework, Long studentId);

    //讲练测推荐学生巩固详情
    @Idempotent
    IntelligentTeachingRecommendResp fetchIntelligentTeachingRecommend(String homeworkId);

    //作业讲练测作业形式统计(用于作业分享讲练测总结部分)
    @Idempotent
    IntelligentTeachingReport fetchIntelligentTeachingReport(String homeworkId);
}
