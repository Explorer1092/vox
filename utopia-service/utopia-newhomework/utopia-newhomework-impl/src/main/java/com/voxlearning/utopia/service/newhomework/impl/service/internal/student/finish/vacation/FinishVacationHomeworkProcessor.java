package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishVacationHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 假期作业Finish
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
@FinishVacationHomeworkTasks({
        FVH_LoadVacationHomeworkResult.class,
        FVH_CheckDuplicated.class,
        FVH_CheckPracticeFinished.class,
        FVH_CalculateScoreAndDuration.class,
        FVH_CheckVacationHomeworkFinished.class,
        FVH_UpdateVacationHomeworkResult.class,
        FVH_CreateJournalStudentHomework.class,
        FVH_Callbacks.class
})
public class FinishVacationHomeworkProcessor extends SpringContainerSupport {
    private final List<FinishVacationHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        FinishVacationHomeworkTasks annotation = getClass().getAnnotation(FinishVacationHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@FinishVacationHomeworkTasks is required");

        for (Class<? extends FinishVacationHomeworkTask> beanClass : annotation.value()) {
            FinishVacationHomeworkTask task = applicationContext.getBean(beanClass);
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public FinishVacationHomeworkContext process(FinishVacationHomeworkContext context) {
        if (context == null) return new FinishVacationHomeworkContext().errorResponse();
        for (FinishVacationHomeworkTask task : tasks) {
            task.execute(context);
            if (!context.isSuccessful()) {
                logger.debug("Task {} return false, terminate", task.getClass().getName());
                break;
            }
            if (context.isTerminateTask()) {
                logger.debug("Task {} set terminateTask true, terminate", task.getClass().getName());
                break;
            }
        }
        return context.clearAdditions();
    }
}
