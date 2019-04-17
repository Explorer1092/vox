package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.business.api.TeachingDiagnosisService;
import com.voxlearning.utopia.business.api.constant.BusinessErrorType;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.TeachingDiagnosisQuestionResult;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.TeachingDiagnosisTask;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.slf4j.Logger;

/**
 * @author songtao
 * @since 2018/2/8
 */
public class TeachingDiagnosisServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(TeachingDiagnosisServiceClient.class);

    @ImportService(interfaceClass = TeachingDiagnosisService.class)
    private TeachingDiagnosisService teachingDiagnosisService;

    public MapMessage fetchPreQuestionsByStudent(StudentDetail student) {
        if (student == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }
        if (student.getClazz() == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NO_CLAZZ.getCode())
                    .setInfo(BusinessErrorType.NO_CLAZZ.getInfo());
        }
        try {
            return  teachingDiagnosisService.fetchPreQuestionsByStudent(student);
        } catch (Exception e) {
            logger.error("fetchPreQuestions error.", e);
            return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo()).setErrorCode(BusinessErrorType.DEFAULT.getCode());
        }
    }

    public MapMessage processPreQuestionResult(PreQuestionResultContext context) {
        if (context == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.PARAMETER_CHECK_ERROR.getCode())
                    .setInfo(BusinessErrorType.PARAMETER_CHECK_ERROR.getInfo());
        }
        if (context.getStudent() == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }
        if (context.getStudent().getClazz() == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NO_CLAZZ.getCode())
                    .setInfo(BusinessErrorType.NO_CLAZZ.getInfo());
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("processPreQuestionResult")
                    .keys(context.getStudent().getId())
                    .callback(() -> {
                        return teachingDiagnosisService.processPreQuestionResult(context);
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(BusinessErrorType.DUPLICATED_OPERATION.getInfo()).setErrorCode(BusinessErrorType.DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed processPreQuestionResult  context:{}", context,  ex);
            return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo()).setErrorCode(BusinessErrorType.DEFAULT.getCode());
        }
    }

    public MapMessage fetchIndexMessage(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.PARAMETER_CHECK_ERROR.getCode())
                    .setInfo(BusinessErrorType.PARAMETER_CHECK_ERROR.getInfo());
        }
        try {
            return teachingDiagnosisService.fetchIndexMessage(taskId);
        } catch (Exception e) {
            logger.error("fetchIndexMessage error. taskId:{}", taskId, e);
            return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo()).setErrorCode(BusinessErrorType.DEFAULT.getCode());
        }
    }

    public MapMessage saveCourseQuestionResult(TeachingDiagnosisQuestionResult questionResult, boolean last) {
        if (questionResult == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.PARAMETER_CHECK_ERROR.getCode())
                    .setInfo(BusinessErrorType.PARAMETER_CHECK_ERROR.getInfo());
        }
        if (questionResult.getStudentId() == null) {
            return MapMessage.errorMessage()
                    .setErrorCode(BusinessErrorType.NEED_LOGIN.getCode())
                    .setInfo(BusinessErrorType.NEED_LOGIN.getInfo());
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("saveCourseQuestionResult")
                    .keys(questionResult.getStudentId())
                    .callback(() -> {
                         return teachingDiagnosisService.saveCourseQuestionResult(questionResult, last);
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            return MapMessage.errorMessage(BusinessErrorType.DUPLICATED_OPERATION.getInfo()).setErrorCode(BusinessErrorType.DUPLICATED_OPERATION.getCode());
        } catch (Exception ex) {
            logger.error("Failed saveCourseQuestionResult  questionResult:{} last:{}", questionResult, last, ex);
            return MapMessage.errorMessage(BusinessErrorType.DEFAULT.getInfo()).setErrorCode(BusinessErrorType.DEFAULT.getCode());
        }
    }

    public TeachingDiagnosisTask fetchDiagnosisTaskCheckedExperimented(Long studentId) {
        if (studentId == null) {
            return null;
        }
        return teachingDiagnosisService.fetchDiagnosisTaskCheckedExperimented(studentId);
    }


    public TeachingDiagnosisTask fetchDiagnosisTaskById(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return null;
        }
        return teachingDiagnosisService.fetchDiagnosisTaskById(taskId);
    }
}
