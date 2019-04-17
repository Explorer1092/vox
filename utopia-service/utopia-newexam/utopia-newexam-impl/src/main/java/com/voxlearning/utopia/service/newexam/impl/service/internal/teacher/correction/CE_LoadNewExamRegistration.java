package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newexam.api.context.CorrectNewExamContext;
import com.voxlearning.utopia.service.newexam.impl.dao.NewExamRegistrationDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Named
public class CE_LoadNewExamRegistration extends SpringContainerSupport implements CorrectNewExamTask {
    @Inject private NewExamRegistrationDao newExamRegistrationDao;

    @Override
    public void execute(CorrectNewExamContext context) {
        context.setNewExamRegistrationMap(newExamRegistrationDao.loads(context.getNewExamResultIds()));
    }
}
