package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.vacation.VacationHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.VacationHomeworkCacheLoaderImpl;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
public class FVH_UpdateVacationHomeworkResult extends SpringContainerSupport implements FinishVacationHomeworkTask {
    @Inject
    private VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject
    private VacationHomeworkCacheLoaderImpl vacationHomeworkCacheLoader;

    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

    @Override
    public void execute(FinishVacationHomeworkContext context) {
        VacationHomeworkResult modified = vacationHomeworkResultDao.finishHomework(
                context.getVacationHomework().toLocation(),
                context.getObjectiveConfigType(),
                context.getPracticeScore(),
                context.getPracticeDuration(),
                context.isPracticeFinished(),
                context.isHomeworkFinished()
        );
        // 如果修改成功并且完成了作业则需要重新对context.result赋值、更新缓存并且发放学豆
        if (modified != null && context.isHomeworkFinished()) {
            // 更新缓存
            vacationHomeworkCacheLoader.addOrModifyVacationHomeworkCacheMapper(context.getVacationHomework());
            // 发放学豆
            int integral = NewHomeworkConstants.FINISH_VACATION_HOMEWORK_INTEGRAL_REWARD; // 学生获得的积分
            String comment = "完成假期作业任务包"; // 评述
            IntegralType integralType = IntegralType.学生完成假期作业任务包; // 积分类型
            try {
                IntegralHistory integralHistory = new IntegralHistory(context.getUserId(), integralType, integral);
                integralHistory.setComment(comment);
                integralHistory.setUniqueKey("vacationHomework:" + context.getVacationHomeworkId());
                userIntegralService.changeIntegral(integralHistory);
            } catch (Exception ex) {
                if (ex instanceof DuplicateKeyException) {
                    logger.warn("Failed to change integral duplicated userId{} vacationHomeworkId{}", context.getUserId(), context.getVacationHomework());
                } else {
                    logger.error("Failed to change integral userId{} vacationHomeworkId{}", context.getUserId(), context.getVacationHomeworkId());
                }
            }
        }
        context.setResult(modified);
        if (!context.isHomeworkFinished()) {
            context.terminateTask();
        }
    }
}
