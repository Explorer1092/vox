package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.correction;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Named
@CorrectHomeworkTasks({
        COH_LoadHomeworkResult.class,
        COH_UpdateCorrect.class,
        COH_ValidateObjectiveConfigTypeFinish.class,
        COH_ValidateHomeworkFinish.class
})
public class CorrectHomeworkProcessor extends SpringContainerSupport {

    private final List<CorrectHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CorrectHomeworkTasks annotation = getClass().getAnnotation(CorrectHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@CorrectHomeworkTasks is required");

        for (Class<? extends CorrectHomeworkTask> beanClass : annotation.value()) {
            CorrectHomeworkTask task = null;
            Map<String, ? extends CorrectHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (CorrectHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public CorrectHomeworkContext process(CorrectHomeworkContext context) {
        if (context == null) return new CorrectHomeworkContext().errorResponse();
        for (CorrectHomeworkTask task : tasks) {
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
