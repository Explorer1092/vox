package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class DH_InitializationHomeworkData extends AbstractHomeworkIndexDataProcessor {

    @Override
    protected void doProcess(HomeworkIndexDataContext context) {
        String homeworkId = context.getHomeworkId();
        Long studentId = context.getStudentId();
        if (StringUtils.isBlank(homeworkId) || null == studentId) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getStudentId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_PARAMETER,
                    "op", "get homework index data"
            ));
            context.errorResponse("作业id或者学生id为空");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
            context.setTerminateTask(true);
            return;
        }
        NewHomework newHomework = newHomeworkLoader.load(context.getHomeworkId());
        if (null == newHomework) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getStudentId(),
                    "mod1", context.getHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST,
                    "op", "get homework index data"
            ));

            context.errorResponse("作业不存在");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }
        context.setNewHomework(newHomework);
        context.setPracticeMap(newHomework.findPracticeContents());

        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(homeworkId);
        if (newHomeworkBook != null) {
            context.setUnitName(StringUtils.join(newHomeworkBook.processUnitNameList(), ","));
        }

        NewHomeworkResult newHomeworkResult = newHomeworkResultLoader.loadNewHomeworkResult(newHomework.toLocation(), studentId, true);
        if (newHomeworkResult != null && newHomeworkResult.getPractices() != null) {
            context.setDoPractices(newHomeworkResult.getPractices());
        }
        context.setNewHomeworkResult(newHomeworkResult);
        context.setFinished(newHomeworkResult != null && newHomeworkResult.isFinished());
        NewAccomplishment newAccomplishment = newAccomplishmentLoader.loadNewAccomplishment(newHomework.toLocation());
        context.setNewAccomplishment(newAccomplishment);
        User user = userLoaderClient.loadUser(newHomework.getTeacherId());
        context.setTeacher(user);
    }
}
