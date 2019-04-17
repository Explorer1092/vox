package com.voxlearning.utopia.service.afenti.impl.service.processor;

import com.voxlearning.utopia.service.afenti.api.context.AbstractAfentiContext;
import com.voxlearning.utopia.service.afenti.impl.util.IAfentiTask;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ruib
 * @since 2016/7/11
 */
public class AbstractAfentiProcessor<T extends AbstractAfentiContext<T>> extends UtopiaAfentiSpringBean {
    private final List<IAfentiTask<T>> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AfentiTasks annotation = getClass().getAnnotation(AfentiTasks.class);
        Objects.requireNonNull(annotation, "@AfentiTasks is required");

        for (Class<? extends IAfentiTask> beanClass : annotation.value()) {
            IAfentiTask<T> task = null;
            Map<String, ? extends IAfentiTask> beans = applicationContext.getBeansOfType(beanClass);
            for (IAfentiTask<T> bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            tasks.add(task);
        }
    }

    public T process(T context) {
        if (context == null) return new AbstractAfentiContext<T>().errorResponse();
        for (IAfentiTask<T> task : tasks) {
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
