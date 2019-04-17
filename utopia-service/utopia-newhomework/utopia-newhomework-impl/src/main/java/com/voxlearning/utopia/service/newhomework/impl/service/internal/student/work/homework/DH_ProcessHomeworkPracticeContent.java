package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template.NewHomeworkIndexDataProcessFactory;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template.NewHomeworkIndexDataProcessTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class DH_ProcessHomeworkPracticeContent extends AbstractHomeworkIndexDataProcessor {

    @Inject
    private NewHomeworkIndexDataProcessFactory newHomeworkIndexDataProcessFactory;

    @Override
    protected void doProcess(HomeworkIndexDataContext context) {
        for (ObjectiveConfigType objectiveConfigType : context.getPracticeMap().keySet()) {
            NewHomeworkIndexDataProcessTemplate template = newHomeworkIndexDataProcessFactory.getTemplate(objectiveConfigType.getNewHomeworkIndexDataProcessTemp());
            context = template.processHomeworkIndexData(context, objectiveConfigType);
            if (context.isTerminateTask()) {
                return;
            }
        }
    }
}
