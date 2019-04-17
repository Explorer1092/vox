package com.voxlearning.utopia.service.newhomework.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.selfstudy.SelfStudyRotReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkReport;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.response.SelfStudyHomeworkDoResp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@ServiceVersion(version = "20180720")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface SelfStudyHomeworkService extends IPingable {

    /* ========= 对dao的封装，业务上尽量不要直接使用dao ========= */

    void insertSelfStudyHomework(SelfStudyHomework entity);

    void insertSelfStudyHomeworks(Collection<SelfStudyHomework> entities);

    void insertSelfStudyHomeworkBook(SelfStudyHomeworkBook entity);

    void insertSelfStudyHomeworkBooks(Collection<SelfStudyHomeworkBook> entities);

    void insertSelfStudyHomeworkResult(SelfStudyHomeworkResult entity);

    void insertSelfStudyHomeworkResults(Collection<SelfStudyHomeworkResult> entities);

    void insertSelfStudyHomeworkReport(SelfStudyHomeworkReport entity);

    void insertSelfStudyHomeworkReports(Collection<SelfStudyHomeworkReport> entities);


    /* ========= 以下是业务使用地方法 ========= */

    /**
     * 生成首页数据
     *
     * @param homeworkId 作业id
     * @param studentId  学生id
     * @return Map
     */
    Map<String, Object> generateIndexData(String homeworkId, Long studentId);

    MapMessage homeworkForObjectiveConfigTypeResult(String homeworkId, ObjectiveConfigType objectiveConfigType, Long studentId);

    Map<String, Object> loadHomeworkQuestions(String homeworkId, ObjectiveConfigType objectiveConfigType, String courseId);

    Map<String, Object> loadHomeworkQuestionsAnswer(String homeworkId, Long studentId, ObjectiveConfigType objectiveConfigType, String courseId);

    MapMessage processorHomeworkResult(SelfStudyHomeworkContext context);

    List<SelfStudyHomeworkDoResp> fetchIntelDiagnosisCourse(String homeworkId, ObjectiveConfigType objectiveConfigType);

    void selfStudyRotReport(SelfStudyRotReport selfStudyRotReport);
}
