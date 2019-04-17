package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.TeachingDiagnosisQuestionResult;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author songtao
 * @since 2018/02/08
 */
@ServiceVersion(version = "20180719")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries()
@CyclopsMonitor("utopia")
public interface TeachingDiagnosisService extends IPingable {

    MapMessage fetchPreQuestionsByStudent(StudentDetail student);

    MapMessage processPreQuestionResult(PreQuestionResultContext context);

    MapMessage fetchIndexMessage(String taskId);

    MapMessage saveCourseQuestionResult(TeachingDiagnosisQuestionResult questionResult, Boolean last);

    List<TeachingDiagnosisTask> fetchTeachingDiagnosisTaskListByUserId(Long studentId);

    /**
     * 检查是否实验过，如果实验都做过，返回最新的结果
     * @param studentId
     * @return
     */
    TeachingDiagnosisTask fetchDiagnosisTaskCheckedExperimented(Long studentId);

    TeachingDiagnosisTask fetchDiagnosisTaskById(String taskId);
}

