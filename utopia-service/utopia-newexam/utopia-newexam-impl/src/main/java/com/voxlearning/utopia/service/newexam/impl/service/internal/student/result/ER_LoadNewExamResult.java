package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamResultDao;
import com.voxlearning.utopia.service.question.api.entity.NewExam;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/14.
 */
@Named
public class ER_LoadNewExamResult extends SpringContainerSupport implements NewExamResultTask{
    @Inject private NewExamResultDao newExamResultDao;

    @Override
    public void execute(NewExamResultContext context) {
        NewExam newExam = context.getNewExam();
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), context.getUserId().toString());

        NewExamResult newExamResult = newExamResultDao.load(id.toString());
        if(newExamResult == null){
            logger.error("newExamResult is null newExamId {}", id.toString());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_RESULT_NOT_EXIST);
            return;
        }

        context.setNewExamResult(newExamResult);
        context.setClazzGroupId(newExamResult.getClazzGroupId());
    }
}
