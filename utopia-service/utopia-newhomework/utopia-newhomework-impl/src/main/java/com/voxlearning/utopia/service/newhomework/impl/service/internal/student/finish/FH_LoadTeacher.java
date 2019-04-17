package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class FH_LoadTeacher extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;

    @Override
    public void execute(FinishHomeworkContext context) {
        //为啥要这样取老师？换班后的作业归属区别？
        List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachersByClazzId(context.getClazzGroup().getClazzId(), context.getUserId());
        ClazzTeacher clazzTeacher = clazzTeachers.stream()
                .filter(ct -> ct.getTeacher().getSubject() != null && ct.getTeacher().getSubject() == context.getHomework().getSubject())
                .findFirst()
                .orElse(null);

        if (clazzTeacher != null && clazzTeacher.getTeacher() != null) {
            context.setTeacherId(clazzTeacher.getTeacher().getId());
            context.setTeacher(clazzTeacher.getTeacher());
        }
    }
}
