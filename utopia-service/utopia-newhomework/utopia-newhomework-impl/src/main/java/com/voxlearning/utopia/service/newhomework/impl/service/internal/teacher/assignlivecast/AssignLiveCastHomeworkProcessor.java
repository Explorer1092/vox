package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignlivecast;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2016/10/18
 */
@Named
@AssignLiveCastHomeworkTasks({
        AHL_CheckRequiredParameters.class,
        AHL_CheckClazzGroup.class,
        AHL_ProcessHomeworkContent.class,
        AHL_SaveHomework.class
})
public class AssignLiveCastHomeworkProcessor extends SpringContainerSupport {
    private final List<AssignLiveCastHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AssignLiveCastHomeworkTasks annotation = getClass().getAnnotation(AssignLiveCastHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@AssignLiveCastHomeworkTasks is required");

        for (Class<? extends AssignLiveCastHomeworkTask> beanClass : annotation.value()) {
            AssignLiveCastHomeworkTask task = null;
            Map<String, ? extends AssignLiveCastHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (AssignLiveCastHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public AssignHomeworkContext process(AssignHomeworkContext context) {
        if (context == null) return new AssignHomeworkContext().errorResponse();
        for (AssignLiveCastHomeworkTask task : tasks) {
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
