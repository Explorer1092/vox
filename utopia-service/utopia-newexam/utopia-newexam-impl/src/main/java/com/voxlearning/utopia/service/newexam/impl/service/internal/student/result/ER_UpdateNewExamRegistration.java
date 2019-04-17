package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.NewExamResultContext;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamRegistrationDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Named
public class ER_UpdateNewExamRegistration extends SpringContainerSupport implements NewExamResultTask {
    @Inject private NewExamRegistrationDao newExamRegistrationDao;

    @Override
    public void execute(NewExamResultContext context) {
        newExamRegistrationDao.doNewExamResult(context.getNewExamResult().getId(), context.getTotalScore(), context.getTotalDureation(), context.isNewExamFinished());
    }
}
