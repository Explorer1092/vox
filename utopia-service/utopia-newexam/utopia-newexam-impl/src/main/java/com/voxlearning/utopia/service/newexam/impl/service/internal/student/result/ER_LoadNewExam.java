package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.entity.StudentExaminationAuthority;
import com.voxlearning.utopia.service.newexam.impl.dao.StudentExaminationAuthorityDao;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Named
public class ER_LoadNewExam extends SpringContainerSupport implements NewExamResultTask{
    @Inject private NewExamLoaderClient newExamLoaderClient;
    @Inject private StudentExaminationAuthorityDao studentExaminationAuthorityDao;
    @Override
    public void execute(NewExamResultContext context) {
        NewExam newExam = newExamLoaderClient.load(context.getNewExamId());
        if (newExam == null) {
            logger.error("NewExam {} not found", context.getNewExamId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            return;
        }
        //考试时间有误的情况
        if (newExam.getExamStopAt() == null ){
            context.errorResponse("考试结束时间不存在");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_COMMON);
            return;
        }
        //考试已经结束的情况
        if (newExam.getExamStopAt().before(new Date())) {
            String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
            NewExamRegistration.ID id = new NewExamRegistration.ID(month, Subject.fromSubjectId(newExam.getSubjectId()), newExam.getId(), context.getUserId().toString());
            StudentExaminationAuthority studentExaminationAuthority = studentExaminationAuthorityDao.load(id.toString());
            //在考试结束的情况时候，没有重考和补考权限的学生没有继续做题
            if (studentExaminationAuthority == null || SafeConverter.toBoolean(studentExaminationAuthority.getDisabled())) {
                context.errorResponse("考试已经结束");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_IS_STOP);
                return;
            }
        }

        context.setNewExam(newExam);
        context.setSubject(Subject.fromSubjectId(newExam.getSubjectId()));

    }
}
