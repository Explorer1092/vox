package com.voxlearning.utopia.service.business.impl.processor;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.business.api.context.AbstractContext;
import com.voxlearning.utopia.service.business.impl.processor.annotation.ExecuteTaskSupport;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 16-10-12
 */
abstract public class AbstractProcessor<T extends AbstractContext<T>> extends SpringContainerSupport implements Processor<T> {

    private final List<ExecuteTask<T>> executeTasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        ExecuteTaskSupport executeTaskSupport = getClass().getAnnotation(ExecuteTaskSupport.class);
        for (Class<? extends ExecuteTask> beanClass : executeTaskSupport.value()) {
            ExecuteTask<T> task = null;
            Map<String, ? extends ExecuteTask> beans = applicationContext.getBeansOfType(beanClass);
            for (ExecuteTask<T> bean : beans.values()) {
                if (bean.getClass() == beanClass) {
                    task = bean;
                    break;
                }
            }
            if (task == null) throw new IllegalStateException("No bean " + beanClass.getName() + " defined");
            executeTasks.add(task);
        }
    }

    @Override
    public T process(T context) {
        if (context == null) return null;
        for (ExecuteTask<T> task : executeTasks) {
            task.execute(context);
            //logger.info("context", context);
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
