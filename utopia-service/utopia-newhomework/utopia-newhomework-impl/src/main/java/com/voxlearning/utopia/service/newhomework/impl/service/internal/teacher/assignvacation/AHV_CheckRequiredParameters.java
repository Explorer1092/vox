package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignvacation;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;

import javax.inject.Named;
import java.util.Date;

/**
 * Created by tanguohong on 2016/11/29.
 */
@Named
public class AHV_CheckRequiredParameters extends SpringContainerSupport implements AssignVacationHomeworkTask {
    @Override
    public void execute(AssignVacationHomeworkContext context) {
        if (context.getTeacher() == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST,
                    "op", "assign vacation homework"
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
                    "op", "assign vacation homework"
            ));
            context.errorResponse("作业内容错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL);
            context.setTerminateTask(true);
            return;
        }

        // TODO: 2018/5/28 YCG    ErrorCodeConstants添加错误类型枚举
        Integer plannedDays = SafeConverter.toInt(homeworkSource.get("plannedDays"));
        if (plannedDays == 0) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_START_TIME,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("作业计划天数错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_START_TIME);
            context.setTerminateTask(true);
            return;
        }
        context.setPlannedDays(plannedDays);

        Date startTime = SafeConverter.toDate(homeworkSource.get("startTime"));
        if (startTime == null
                || startTime.getTime() < NewHomeworkConstants.earliestVacationHomeworkStartDate(RuntimeMode.current()).getTime()
                || startTime.getTime() > NewHomeworkConstants.VH_START_DATE_LATEST.getTime()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_START_TIME,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("作业起始时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_START_TIME);
            context.setTerminateTask(true);
            return;
        }
        context.setHomeworkStartTime(startTime);

        Date endTime = SafeConverter.toDate(homeworkSource.get("endTime"));
        if (endTime == null
                || endTime.before(startTime)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_ENT_TIME,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("截止时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ENT_TIME);
            context.setTerminateTask(true);
            return;
        }

        if (endTime.after(NewHomeworkConstants.VH_END_DATE_LATEST)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_ENT_TIME,
                    "op", "assign vacation homework"
            ));
            context.errorResponse("截止时间不得超过3月17日23:59");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ENT_TIME);
            context.setTerminateTask(true);
            return;
        }

        context.setHomeworkEndTime(endTime);

        if (homeworkSource.containsKey("remark")) {
            context.setRemark(SafeConverter.toString(homeworkSource.get("remark")));
        }
    }
}
