package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.VacationHomeworkCacheLoader;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkCacheMapper;
import com.voxlearning.utopia.service.newhomework.api.mapper.vacation.VacationHomeworkDetailCacheMapper;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_CheckDuplicated extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Inject
    private VacationHomeworkCacheLoader vacationHomeworkCacheLoader;

    @Override
    public void execute(FinishVacationHomeworkContext context) {
        VacationHomeworkCacheMapper vacationHomeworkCacheMapper = vacationHomeworkCacheLoader.loadVacationHomeworkCacheMapper(context.getClazzGroupId(), context.getUserId());
        if (vacationHomeworkCacheMapper != null && MapUtils.isNotEmpty(vacationHomeworkCacheMapper.getHomeworkDetail())) {
            VacationHomeworkDetailCacheMapper vacationHomeworkDetailCacheMapper = vacationHomeworkCacheMapper.getHomeworkDetail().get(context.getVacationHomeworkId());
            if (vacationHomeworkDetailCacheMapper != null && vacationHomeworkDetailCacheMapper.isFinished()) {
                logger.debug("Student {} already finished vacation homework {}, ignore", context.getUserId(), context.getVacationHomework().toLocation());
                context.setTerminateTask(true);
            }
        }
    }
}
