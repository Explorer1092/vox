package com.voxlearning.utopia.service.newhomework.impl.template.assign;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
abstract public class AssignHomeworkTemplate extends NewHomeworkSpringBean {

    abstract public NewHomeworkType getNewHomeworkType();

    abstract public AssignHomeworkContext assignHomework(Teacher teacher, HomeworkSource homeworkSource, HomeworkSourceType homeworkSourceType, NewHomeworkType newHomeworkType, HomeworkTag homeworkTag);
}
