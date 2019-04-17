package com.voxlearning.utopia.service.ai.impl.service.processor;


import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.ai.context.AbstractAIContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AbstractAIProcessor<T extends AbstractAIContext<T>> extends SpringContainerSupport {
    private final List<IAITask<T>> tasks = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AITask annotation = getClass().getAnnotation(AITask.class);
        Objects.requireNonNull(annotation, "@AITasks is required");

        for (Class<? extends IAITask> beanClass : annotation.value()) {
            IAITask<T> task = null;
            Map<String, ? extends IAITask> beans = applicationContext.getBeansOfType(beanClass);
            for (IAITask<T> bean : beans.values()) {
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
        if (context == null) return new AbstractAIContext<T>().errorResponse();
        for (IAITask<T> task : tasks) {
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

