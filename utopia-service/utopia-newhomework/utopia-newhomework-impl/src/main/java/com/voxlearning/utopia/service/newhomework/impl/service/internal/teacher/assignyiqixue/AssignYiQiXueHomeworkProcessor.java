package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignyiqixue;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.context.AssignLiveCastHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2016/12/16
 */
@Named
@AssignYiQiXueHomeworkTasks({
        AHYQX_CheckRequiredParameters.class,
        AHYQX_CheckClazzGroup.class,
        AHYQX_ProcessHomeworkContent.class,
        AHYQX_SaveHomework.class,
        AHYQX_Callback.class,
        AHYQX_PublishMessage.class
})
public class AssignYiQiXueHomeworkProcessor extends SpringContainerSupport {
    private final List<AssignYiQiXueHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AssignYiQiXueHomeworkTasks annotation = getClass().getAnnotation(AssignYiQiXueHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@AssignYiQiXueHomeworkTasks is required");

        for (Class<? extends AssignYiQiXueHomeworkTask> beanClass : annotation.value()) {
            AssignYiQiXueHomeworkTask task = null;
            Map<String, ? extends AssignYiQiXueHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (AssignYiQiXueHomeworkTask bean : beans.values()) {
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
        for (AssignYiQiXueHomeworkTask task : tasks) {
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
