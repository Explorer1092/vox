package com.voxlearning.utopia.service.newhomework.impl.template.assign;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.AssignHomeworkProcessor;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by tanguohong on 2017/4/28.
 */
@Named
public class HomeworkTermReview extends AssignHomeworkTemplate {

    @Inject
    private AssignHomeworkProcessor assignHomeworkProcessor;

    @Override
    public NewHomeworkType getNewHomeworkType() {
        return NewHomeworkType.TermReview;
    }

    @Override
    public AssignHomeworkContext assignHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag) {
        AssignHomeworkContext assignHomeworkContext = new AssignHomeworkContext();
        assignHomeworkContext.setSource(homeworkSource);
        assignHomeworkContext.setHomeworkSourceType(homeworkSourceType);
        assignHomeworkContext.setTeacher(teacher);
        assignHomeworkContext.setNewHomeworkType(NewHomeworkType.TermReview);
        assignHomeworkContext.setHomeworkTag(homeworkTag);

        return assignHomeworkProcessor.process(assignHomeworkContext);
    }
}
