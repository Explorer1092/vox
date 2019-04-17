package com.voxlearning.utopia.service.newhomework.impl.template.assign;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.AssignSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy.AssignSelfStudyHomeworkTaskProcessor;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Named
public class HomeworkSelfStudy extends AssignHomeworkTemplate {

    @Inject private AssignSelfStudyHomeworkTaskProcessor assignSelfStudyHomeworkTaskProcessor;

    @Override
    public NewHomeworkType getNewHomeworkType() {
        return NewHomeworkType.selfstudy;
    }

    @Override
    public AssignHomeworkContext assignHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag) {
        AssignSelfStudyHomeworkContext assignHomeworkContext = new AssignSelfStudyHomeworkContext();
        assignHomeworkContext.setSource(homeworkSource);
        assignHomeworkContext.setHomeworkSourceType(homeworkSourceType);
        assignHomeworkContext.setNewHomeworkType(NewHomeworkType.selfstudy);
        assignHomeworkContext.setHomeworkTag(homeworkTag);

        assignHomeworkContext = assignSelfStudyHomeworkTaskProcessor.process(assignHomeworkContext);
        AssignHomeworkContext context = new AssignHomeworkContext();
        context.setHomeworkIds(Collections.singletonList(assignHomeworkContext.getHomeworkId()));
        return context;
    }
}
