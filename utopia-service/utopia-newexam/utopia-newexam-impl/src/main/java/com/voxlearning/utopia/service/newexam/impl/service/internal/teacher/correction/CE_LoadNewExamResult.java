package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.api.entity.StudentExaminationAuthority;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamResultDao;
import com.voxlearning.utopia.service.newexam.impl.dao.StudentExaminationAuthorityDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
public class CE_LoadNewExamResult extends SpringContainerSupport implements CorrectNewExamTask {

    @Inject private NewExamResultDao newExamResultDao;
    @Inject private StudentExaminationAuthorityDao studentExaminationAuthorityDao;

    @Override
    public void execute(CorrectNewExamContext context) {
        Map<String, NewExamResult> newExamResultMap = newExamResultDao.loads(context.getNewExamResultIds());
        if (newExamResultMap.size() != 1) {
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_MULTIPLE_CORRECT_USER);
            return;
        }
        NewExamResult newExamResult1 = newExamResultMap.values().iterator().next();
        //学生未提交的情况
        if (newExamResult1.getSubmitAt() == null) {
            StudentExaminationAuthority studentExaminationAuthority = studentExaminationAuthorityDao.load(newExamResult1.getId());
            //如果该学生是还有重考和补考的权限的，那没法批改，显示未交卷
            if (studentExaminationAuthority != null && !SafeConverter.toBoolean(studentExaminationAuthority.getDisabled())) {
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_NEWEXAM_UN_SUBMIT);
                context.errorResponse("学生未交卷");
            }
        }
        context.setNewExamResultMap(newExamResultMap);
        List<String> newExamProcessResults = context.getNewExamResultMap().values().stream().map(newExamResult -> newExamResult.getAnswers().get(context.getNewQuestion().getDocId())).collect(Collectors.toList());
        context.setNewExamProcessResultIds(newExamProcessResults);
    }
}
