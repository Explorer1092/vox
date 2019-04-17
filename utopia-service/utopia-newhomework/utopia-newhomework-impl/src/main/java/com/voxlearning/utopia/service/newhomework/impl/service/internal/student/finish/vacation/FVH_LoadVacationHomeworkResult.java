package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_LoadVacationHomeworkResult extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Inject
    private VacationHomeworkResultDao vacationHomeworkResultDao;

    @Override
    public void execute(FinishVacationHomeworkContext context) {
        VacationHomework.Location location = context.getVacationHomework().toLocation();
        VacationHomeworkResult.ID id = new VacationHomeworkResult.ID(
                location.getPackageId(),
                location.getWeekRank(),
                location.getDayRank(),
                context.getUserId());
        VacationHomeworkResult result = vacationHomeworkResultDao.load(id.toString());

        if (result == null) {
            logger.error("Cannot locate student {}'s VacationHomeworkResult {}", context.getUserId(), id.toString());
            context.errorResponse();
            return;
        }
        context.setResult(result);
    }
}
