package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;

/**
 * 获取假期作业信息
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_LoadVacationHomework extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private VacationHomeworkLoaderImpl vacationHomeworkLoader;
    @Inject
    private VacationHomeworkResultDao vacationHomeworkResultDao;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        VacationHomework vacationHomework = vacationHomeworkLoader.loadVacationHomeworkIncludeDisabled(context.getVacationHomeworkId());
        if (vacationHomework == null) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST,
                    "op", "student vacation homework result"
            ));
            //logger.error("VacationHomework {} not found", context.getVacationHomeworkId());
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_VACATION_HOMEWORK_NOT_EXIST);
            return;
        }

        Subject subject = vacationHomework.getSubject();
        if (Objects.equals(subject, Subject.UNKNOWN)) {
            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", context.getUserId(),
                    "mod1", context.getVacationHomeworkId(),
                    "mod2", ErrorCodeConstants.ERROR_CODE_SUBJECT,
                    "op", "student vacation homework result"
            ));
            logger.error("Cannot recognize VacationHomework subject {}", subject);
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            return;
        }

        context.setVacationHomework(vacationHomework);
        context.setNewHomeworkType(vacationHomework.getType());
        context.setClazzGroupId(vacationHomework.getClazzGroupId());
        context.setSubject(subject);

        VacationHomeworkResult vacationHomeworkResult = vacationHomeworkResultDao.load(vacationHomework.getId());
        if (vacationHomeworkResult == null) {
            vacationHomeworkResultDao.initVacationHomeworkResult(vacationHomework.toLocation(), context.getUserId());
        }
    }
}
