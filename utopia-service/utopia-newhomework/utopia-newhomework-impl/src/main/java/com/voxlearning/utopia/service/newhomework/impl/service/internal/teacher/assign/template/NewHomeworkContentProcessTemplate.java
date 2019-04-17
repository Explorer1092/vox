package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.template;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkContentProcessTemp;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.util.Map;

/**
 * 生成作业内容
 * Created by tanguohong on 16/7/7.
 */
abstract public class NewHomeworkContentProcessTemplate extends NewHomeworkSpringBean {

    abstract public NewHomeworkContentProcessTemp getNewHomeworkContentTemp();

    abstract public AssignHomeworkContext processHomeworkContent(AssignHomeworkContext context, Map<String, Object> practice, ObjectiveConfigType objectiveConfigType);

    protected AssignHomeworkContext contentError(AssignHomeworkContext context, ObjectiveConfigType objectiveConfigType) {
        LogCollector.info("backend-general", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "usertoken", context.getTeacher().getId(),
                "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT,
                "mod3", JsonUtils.toJson(context.getSource()),
                "op", "assign homework"
        ));
        context.errorResponse("{}内容错误", objectiveConfigType.getValue());
        context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT);
        context.setTerminateTask(true);
        return context;
    }
}
