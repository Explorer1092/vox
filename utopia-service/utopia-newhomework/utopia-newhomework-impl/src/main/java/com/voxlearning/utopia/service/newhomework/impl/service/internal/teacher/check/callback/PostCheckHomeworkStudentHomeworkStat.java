package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostCheckHomeworkStudentHomeworkStat extends SpringContainerSupport implements PostCheckHomework {
    @Inject private NewHomeworkServiceImpl homeworkService;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        NewAccomplishment accomplishment = context.getAccomplishment();
        if (accomplishment == null || accomplishment.size() <= 0) return;

        for (String studentId : accomplishment.getDetails().keySet()) {
            // 记录学生在老师的班级中完成作业次数，检查作业加一部分，补做作业加一部分
            homeworkService.incFinishHomeworkCount(context.getTeacherId(),
                    context.getClazzId(), SafeConverter.toLong(studentId));
        }
    }
}
