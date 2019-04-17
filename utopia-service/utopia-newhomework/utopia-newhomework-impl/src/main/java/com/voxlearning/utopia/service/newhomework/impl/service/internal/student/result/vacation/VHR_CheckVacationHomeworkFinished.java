package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @Description: 假期作业是否完成
 * @author: Mr_VanGogh
 * @date: 2018/8/8 上午10:45
 */
@Named
public class VHR_CheckVacationHomeworkFinished extends SpringContainerSupport implements VacationHomeworkResultTask {

    @Inject
    private VacationHomeworkResultDao vacationHomeworkResultDao;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        VacationHomework vacationHomework = context.getVacationHomework();
        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(vacationHomework.getId());

        boolean finished = vacationHomeworkResult != null && vacationHomeworkResult.getFinishAt() != null;
        if (finished) {
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_FINISHED,
                    "mod3", context.getObjectiveConfigType(),
                    "mod4", JsonUtils.toJson(context.getStudentHomeworkAnswers()),
                    "op", "student vacation homework result"
            ));
            context.errorResponse("假期作业已完成");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_IS_FINISHED);
        }
    }
}
