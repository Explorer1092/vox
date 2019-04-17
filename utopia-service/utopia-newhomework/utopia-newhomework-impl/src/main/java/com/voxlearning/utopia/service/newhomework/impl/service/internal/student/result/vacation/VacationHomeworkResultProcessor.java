package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.vacation;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.VacationHomeworkResultContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author guoqiang.li
 * @since 2016/12/7
 */
@Named
@VacationHomeworkResultTasks({
        VHR_LoadVacationHomework.class,
        VHR_CheckVacationHomeworkFinished.class,
        VHR_LoadGroup.class,
        VHR_LoadClazz.class,
        VHR_CalculateStandardScore.class,
        VHR_CalculateScore.class,
        VHR_ProcessFile.class,
        VHR_CreateVacationHomeworkProcessResult.class,
        VHR_UpdateVacationHomeworkResult.class,
        VHR_CheckBasicAppPracticeFinish.class,
        VHR_CreateStudentExamResult.class,
        VHR_FinishVacationHomework.class
})
public class VacationHomeworkResultProcessor extends SpringContainerSupport {
    private final List<VacationHomeworkResultTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        VacationHomeworkResultTasks annotation = getClass().getAnnotation(VacationHomeworkResultTasks.class);
        Objects.requireNonNull(annotation, "@VacationHomeworkResultTasks is required");
        for (Class<? extends VacationHomeworkResultTask> beanClass : annotation.value()) {
            VacationHomeworkResultTask task = applicationContext.getBean(beanClass);
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public VacationHomeworkResultContext process(VacationHomeworkResultContext context) {
        if (context == null) return new VacationHomeworkResultContext().errorResponse();
        for (VacationHomeworkResultTask task : tasks) {
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
