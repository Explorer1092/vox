package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkLoaderImpl;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Named
public class CH_LoadHomework extends SpringContainerSupport implements CheckHomeworkTask {
    @Inject private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Override
    public void execute(CheckHomeworkContext context) {
        if (context.getTeacher() == null || StringUtils.isBlank(context.getHomeworkId()) || context.getCheckHomeworkSource() == null) {
            context.errorResponse();
            return;
        }
        NewHomework homework = newHomeworkLoader.loadNewHomework(context.getHomeworkId());
        if (homework == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST,
                    "op", "teacher check homework"
            ));
            logger.error("NewHomework {} not found", context.getHomeworkId());
            context.errorResponse("作业不存在");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            return;
        }

        if (context.getTeacher().getSubjects().size() > 1) {// 包班制支持，根据当前作业学科，找到对应的老师id，重新设置当前上下文的老师信息
            Long relTeacherId = teacherLoaderClient.loadRelTeacherIdBySubject(context.getTeacher().getId(), homework.getSubject());
            if (!Objects.equals(relTeacherId, context.getTeacher().getId())) {
                context.setTeacher(teacherLoaderClient.loadTeacher(relTeacherId));
            }
        }

        if (context.getTeacher() == null) {
            context.errorResponse("老师信息错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST);
            return;
        }

        context.setTeacherId(context.getTeacher().getId());
        if (homework.getSubject() != context.getTeacher().getSubject()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
                    "op", "teacher check homework"
            ));
            context.errorResponse("学科错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            return;
        }
        if (homework.isHomeworkChecked()) {
            context.errorResponse("作业已经检查完毕");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_CHECKED);
            return;
        }

        context.setHomework(homework);
        context.setHomeworkType(HomeworkType.of(homework.getSubject().name()));
    }
}
