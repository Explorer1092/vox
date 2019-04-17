package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/14
 */
@Named
@FinishHomeworkTasks({
        FH_LoadHomeworkResult.class,
        FH_LoadTeacher.class,
        FH_CheckDuplicated.class,
        FH_CheckPracticeFinished.class,
        FH_CalculateScoreAndDuration.class,
        FH_CheckHomeworkFinished.class,
        FH_UpdateHomeworkResult.class,
        FH_ClassifyImage.class,
        FH_UpdateBasicReviewHomeworkCache.class,
        FH_CreateBasicReviewHomeworkReport.class,
        FH_CreateJournalStudentHomework.class,
        FH_Callbacks.class
})
public class FinishHomeworkProcessor extends SpringContainerSupport {
    private final List<FinishHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        FinishHomeworkTasks annotation = getClass().getAnnotation(FinishHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@FinishHomeworkTasks is required");

        for (Class<? extends FinishHomeworkTask> beanClass : annotation.value()) {
            FinishHomeworkTask task = null;
            Map<String, ? extends FinishHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (FinishHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public FinishHomeworkContext process(FinishHomeworkContext context) {
        if (context == null) return new FinishHomeworkContext().errorResponse();
        for (FinishHomeworkTask task : tasks) {
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
