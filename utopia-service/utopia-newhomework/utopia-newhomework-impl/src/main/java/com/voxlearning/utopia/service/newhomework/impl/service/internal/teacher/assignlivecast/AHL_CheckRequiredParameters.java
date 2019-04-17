package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignlivecast;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;

import javax.inject.Named;
import java.util.Date;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Named
public class AHL_CheckRequiredParameters extends SpringContainerSupport implements AssignLiveCastHomeworkTask {

    @Override
    public void execute(AssignHomeworkContext context) {
        if (context.getTeacher() == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST,
                    "op", "assign homework"
            ));
            context.errorResponse("老师信息错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }

        HomeworkSource homeworkSource = context.getSource();
        if (homeworkSource == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL,
                    "op", "assign homework"
            ));
            context.errorResponse("作业内容错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL);
            context.setTerminateTask(true);
            return;
        }

        if (context.getSource().get("subject") == null || context.getTeacher().getSubject() != Subject.of(SafeConverter.toString(context.getSource().get("subject")))) {
            // context.errorResponse("subject is null or teacher's subject not equal homework's subject: {}", context.getSource().get("subject"));
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
                    "op", "assign homework"
            ));
            context.errorResponse("请重新登录或联系客服");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            context.setTerminateTask(true);
            return;
        }

        long checkStartTime = DayRange.current().getStartTime();
        long checkEndTime = DayRange.current().getEndTime();
        long currentTime = new Date().getTime();

        Date startTime = SafeConverter.toDate(homeworkSource.get("startTime"));
        if (startTime == null
                || startTime.getTime() < checkStartTime
                || startTime.getTime() > checkEndTime) {
            context.errorResponse("作业起始时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_START_TIME);
            context.setTerminateTask(true);
            return;
        }
        context.setHomeworkStartTime(DateUtils.stringToDate(SafeConverter.toString(homeworkSource.get("startTime"))));

        Date endTime = SafeConverter.toDate(homeworkSource.get("endTime"));
        if (endTime == null
                || endTime.getTime() < currentTime
                || startTime.getTime() > endTime.getTime()) {
            context.errorResponse("作业结束时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ENT_TIME);
            context.setTerminateTask(true);
            return;
        }
        context.setHomeworkEndTime(DateUtils.stringToDate(SafeConverter.toString(homeworkSource.get("endTime"))));

        Long duration = SafeConverter.toLong(homeworkSource.get("duration"));
        if (duration == 0) {
            context.errorResponse("作业预计时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DURATION);
            context.setTerminateTask(true);
            return;
        }
        context.setDuration(duration);
    }
}
