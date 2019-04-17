package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AssignSelfStudyHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Named
@AssignSelfStudyHomeworkTasks({
        AHSS_CheckRequiredParameters.class,
        AHSS_ProcessHomeworkContent.class,
        AHSS_SaveHomework.class,
        AHSS_SaveHomeworkSelfStudyRef.class
})
public class AssignSelfStudyHomeworkTaskProcessor extends SpringContainerSupport {
    private final List<AssignSelfStudyHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AssignSelfStudyHomeworkTasks annotation = getClass().getAnnotation(AssignSelfStudyHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@AssignSelfStudyHomeworkTasks is required");

        for (Class<? extends AssignSelfStudyHomeworkTask> beanClass : annotation.value()) {
            AssignSelfStudyHomeworkTask task = null;
            Map<String, ? extends AssignSelfStudyHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (AssignSelfStudyHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public AssignSelfStudyHomeworkContext process(AssignSelfStudyHomeworkContext context) {
        if (context == null) return new AssignSelfStudyHomeworkContext().errorResponse();
        for (AssignSelfStudyHomeworkTask task : tasks) {
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
