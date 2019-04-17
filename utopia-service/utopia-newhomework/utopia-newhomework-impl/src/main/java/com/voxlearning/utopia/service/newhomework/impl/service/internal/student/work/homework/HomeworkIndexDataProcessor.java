package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.alps.lang.util.SpringContainerSupport;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
@HomeworkIndexDataChain({
        DH_InitializationHomeworkData.class,
        DH_ProcessHomeworkPracticeContent.class,
        DH_ProcessHomeworkIndexData.class,
        DH_ProcessHomeworkRepairData.class
})
public class HomeworkIndexDataProcessor extends SpringContainerSupport {
    private final List<AbstractHomeworkIndexDataProcessor> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        HomeworkIndexDataChain annotation = getClass().getAnnotation(HomeworkIndexDataChain.class);
        for (Class<? extends AbstractHomeworkIndexDataProcessor> beanClass : annotation.value()) {
            AbstractHomeworkIndexDataProcessor loader = getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException("Bean " + beanClass.getName() + " not found");
            }
            chains.add(loader);
        }
        logger.debug("Composed {} homework index data chains", chains.size());
    }

    public HomeworkIndexDataContext process(HomeworkIndexDataContext context) {
        if (context == null) return new HomeworkIndexDataContext().errorResponse();
        for (AbstractHomeworkIndexDataProcessor task : chains) {
            task.process(context);
            if (context.isTerminateTask()) {
                logger.debug("Task {} set terminateTask true, terminate", task.getClass().getName());
                break;
            }
        }
        return context.clearAdditions();
    }
}
