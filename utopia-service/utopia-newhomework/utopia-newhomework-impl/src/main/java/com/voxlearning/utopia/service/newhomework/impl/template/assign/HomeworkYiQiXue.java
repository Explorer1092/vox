package com.voxlearning.utopia.service.newhomework.impl.template.assign;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.AssignLiveCastHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.livecast.LiveCastHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue.AssignYiQiXueHomeworkProcessor;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2017/7/10.
 */
@Named
public class HomeworkYiQiXue extends AssignHomeworkTemplate {

    @Inject
    private AssignYiQiXueHomeworkProcessor assignYiQiXueHomeworkProcessor;

    @Override
    public NewHomeworkType getNewHomeworkType() {
        return NewHomeworkType.YiQiXue;
    }

    @Override
    public AssignHomeworkContext assignHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag) {
        AssignHomeworkContext assignHomeworkContext = new AssignHomeworkContext();
        assignHomeworkContext.setSource(homeworkSource);
        assignHomeworkContext.setHomeworkSourceType(homeworkSourceType);
        assignHomeworkContext.setTeacher(teacher);
        assignHomeworkContext.setNewHomeworkType(NewHomeworkType.YiQiXue);
        assignHomeworkContext.setHomeworkTag(homeworkTag);

        assignHomeworkContext = assignYiQiXueHomeworkProcessor.process(assignHomeworkContext);
        List<String> homeworkIds = assignHomeworkContext.getAssignedGroupHomework().values().stream().map(NewHomework::getId).collect(Collectors.toList());
        AssignHomeworkContext context = new AssignHomeworkContext();
        context.setHomeworkIds(homeworkIds);

        return context;
    }
}
