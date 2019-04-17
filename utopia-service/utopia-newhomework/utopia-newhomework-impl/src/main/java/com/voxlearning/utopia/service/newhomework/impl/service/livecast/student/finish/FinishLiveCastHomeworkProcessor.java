package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.FinishLiveCastHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
@FinishLiveCastHomeworkTasks({
        LCFH_LoadHomeworkResult.class,
        LCFH_CheckPracticeFinished.class,
        LCFH_CalculateScoreAndDuration.class,
        LCFH_CheckHomeworkFinished.class,
        LCFH_UpdateHomeworkResult.class
})
public class FinishLiveCastHomeworkProcessor extends SpringContainerSupport {
    private final List<FinishLiveCastHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        FinishLiveCastHomeworkTasks annotation = getClass().getAnnotation(FinishLiveCastHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@FinishLiveCastHomeworkTasks is required");

        for (Class<? extends FinishLiveCastHomeworkTask> beanClass : annotation.value()) {
            FinishLiveCastHomeworkTask task = null;
            Map<String, ? extends FinishLiveCastHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (FinishLiveCastHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public FinishLiveCastHomeworkContext process(FinishLiveCastHomeworkContext context) {
        if (context == null) return new FinishLiveCastHomeworkContext().errorResponse();
        for (FinishLiveCastHomeworkTask task : tasks) {
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
