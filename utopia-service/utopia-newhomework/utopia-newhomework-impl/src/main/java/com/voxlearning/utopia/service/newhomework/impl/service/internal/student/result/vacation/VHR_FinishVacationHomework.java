package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation.FinishVacationHomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 作业完成责任链
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class VHR_FinishVacationHomework extends SpringContainerSupport implements VacationHomeworkResultTask {
    @Inject
    private FinishVacationHomeworkProcessor finishVacationHomeworkProcessor;

    @Override
    public void execute(VacationHomeworkResultContext context) {
        FinishVacationHomeworkContext ctx = new FinishVacationHomeworkContext();
        ctx.setUserId(context.getUserId());
        ctx.setUser(context.getUser());
        ctx.setClazz(context.getClazz());
        ctx.setClazzId(context.getClazzId());
        ctx.setClazzGroupId(context.getClazzGroupId());
        ctx.setClazzGroup(context.getClazzGroup());
        ctx.setVacationHomeworkId(context.getVacationHomeworkId());
        ctx.setVacationHomework(context.getVacationHomework());
        ctx.setNewHomeworkType(context.getNewHomeworkType());
        ctx.setObjectiveConfigType(context.getObjectiveConfigType());
        ctx.setClientType(context.getClientType());
        ctx.setClientName(context.getClientName());
        ctx.setIpImei(context.getIpImei());
        ctx.setSupplementaryData(false);
        AlpsThreadPool.getInstance().submit(() -> finishVacationHomeworkProcessor.process(ctx));
    }
}
