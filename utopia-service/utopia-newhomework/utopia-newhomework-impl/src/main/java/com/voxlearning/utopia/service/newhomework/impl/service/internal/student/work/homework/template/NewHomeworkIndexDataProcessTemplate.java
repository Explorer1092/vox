package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.template;

import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework.HomeworkIndexDataContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkIndexDataProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
abstract public class NewHomeworkIndexDataProcessTemplate extends NewHomeworkSpringBean {

    abstract public NewHomeworkIndexDataProcessTemp getNewHomeworkIndexDataTemp();

    abstract public HomeworkIndexDataContext processHomeworkIndexData(HomeworkIndexDataContext context, ObjectiveConfigType objectiveConfigType);
}
