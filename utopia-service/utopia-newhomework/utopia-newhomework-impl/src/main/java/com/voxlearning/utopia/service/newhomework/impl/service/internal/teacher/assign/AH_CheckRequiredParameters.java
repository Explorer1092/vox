package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
public class AH_CheckRequiredParameters extends AbstractAssignHomeworkProcessor {
    @Override
    @SuppressWarnings("unchecked")
    protected void doProcess(AssignHomeworkContext context) {
        if (context.getTeacher() == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", "null",
                    "mod2", ErrorCodeConstants.ERROR_CODE_TEACHER_NOT_EXIST,
                    "mod3", JsonUtils.toJson(context.getSource()),
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

        if (context.getHomeworkSourceType() == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_SOURCE_TYPE_IS_NULL,
                    "mod3", JsonUtils.toJson(context.getSource()),
                    "op", "assign homework"
            ));
            context.errorResponse("作业来源错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_SOURCE_TYPE_IS_NULL);
            context.setTerminateTask(true);
            return;
        }

        if (context.getSource().get("subject") == null || context.getTeacher().getSubject() != Subject.of(SafeConverter.toString(context.getSource().get("subject")))) {
            // context.errorResponse("subject is null or teacher's subject not equal homework's subject: {}", context.getSource().get("subject"));
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
                    "mod3", JsonUtils.toJson(context.getSource()),
                    "op", "assign homework"
            ));
            context.errorResponse("登录超时，请重新登录");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            context.setTerminateTask(true);
            return;
        }

        Date currentTime = new Date();
        context.setHomeworkStartTime(currentTime);

        Date endTime = SafeConverter.toDate(homeworkSource.get("endTime"));
        if (endTime == null
                || currentTime.getTime() > endTime.getTime()) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_ENT_TIME,
                    "mod3", JsonUtils.toJson(context.getSource()),
                    "op", "assign homework"
            ));
            context.errorResponse("作业结束时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_ENT_TIME);
            context.setTerminateTask(true);
            return;
        }
        context.setHomeworkEndTime(endTime);

        long effectiveTime = endTime.getTime() - currentTime.getTime();
        if (effectiveTime >= NewHomeworkConstants.MAX_EFFECTIVE_MILLISECONDS) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getTeacher().getId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_EFFECTIVE_TIME_ERROR,
                    "mod3", JsonUtils.toJson(context.getSource()),
                    "op", "assign homework"
            ));
            context.errorResponse("作业有效期超过1年，请重新设置");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_EFFECTIVE_TIME_ERROR);
            context.setTerminateTask(true);
            return;
        }

        if (homeworkSource.containsKey("durations")) {
            List<Map<String, Object>> durations = (List<Map<String, Object>>) homeworkSource.get("durations");
            if (CollectionUtils.isNotEmpty(durations)) {
                for (Map<String, Object> map : durations) {
                    Long groupId = SafeConverter.toLong(map.get("groupId"));
                    Long seconds = SafeConverter.toLong(map.get("seconds"));
                    if (seconds <= 0) {
                        LogCollector.info("backend-general", MiscUtils.map(
                                "env", RuntimeMode.getCurrentStage(),
                                "usertoken", context.getTeacher().getId(),
                                "mod2", ErrorCodeConstants.ERROR_CODE_DURATION,
                                "mod3",JsonUtils.toJson(context.getSource()),
                                "op", "assign homework"
                        ));
                        context.errorResponse("作业预计时间错误");
                        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DURATION);
                        context.setTerminateTask(true);
                        return;
                    }
                    context.getGroupDurations().put(groupId, seconds);
                }
            }
        } else {
            Long duration = SafeConverter.toLong(homeworkSource.get("duration"));
            if (duration <= 0) {
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "usertoken", context.getTeacher().getId(),
                        "mod2", ErrorCodeConstants.ERROR_CODE_DURATION,
                        "mod3", JsonUtils.toJson(context.getSource()),
                        "op", "assign homework"
                ));
                context.errorResponse("作业预计时间错误");
                context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DURATION);
                context.setTerminateTask(true);
                return;
            }
            context.setDuration(duration);
        }
    }
}
