package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Named
@CheckHomeworkTasks({
        CH_LoadHomework.class,
        CH_LoadClazzGroup.class,
        CH_LoadStudents.class,
        CH_LoadAccomplishment.class,
        CH_CalculateIntegral.class,
        CH_QuantityInspector.class,
        //CH_CheatingInspector.class, // 作弊判断暂时下线
        CH_CacheCheckHomeworkIntegralDetail.class,
        //CH_RecordCheatingTeacher.class, // 作弊判断暂时下线
        CH_IncreaseIntegral.class,
        CH_IncreaseClazzIntegral.class,
        CH_IncreaseExpiredIntegral.class,
        CH_UpdateHomeworkChecked.class,
        CH_PostCheckHomeworkCallbacks.class
})
public class CheckHomeworkProcessor extends SpringContainerSupport {
    private final List<CheckHomeworkTask> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CheckHomeworkTasks annotation = getClass().getAnnotation(CheckHomeworkTasks.class);
        Objects.requireNonNull(annotation, "@CheckHomeworkTasks is required");

        for (Class<? extends CheckHomeworkTask> beanClass : annotation.value()) {
            CheckHomeworkTask task = null;
            Map<String, ? extends CheckHomeworkTask> beans = applicationContext.getBeansOfType(beanClass);
            for (CheckHomeworkTask bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public CheckHomeworkContext process(CheckHomeworkContext context) {
        if (context == null) return new CheckHomeworkContext().errorResponse();
        for (CheckHomeworkTask task : tasks) {
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
