package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
public class CE_LoadNewExam extends SpringContainerSupport implements CorrectNewExamTask {

    @Inject private NewExamLoaderClient newExamLoaderClient;

    @Override
    public void execute(CorrectNewExamContext context) {
        NewExam newExam = newExamLoaderClient.load(context.getNewExamId());
        if (newExam == null) {
            logger.error("NewExam {} not found", context.getNewExamId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_NOT_EXIST);
            return;
        }
        context.setNewExam(newExam);

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        List<String> newExamResultIds = new ArrayList<>();
        for(Long userId : context.getUserScoreMap().keySet()){
            NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), SafeConverter.toString(userId));
            newExamResultIds.add(id.toString());
        }
        context.setNewExamResultIds(newExamResultIds);
    }
}
