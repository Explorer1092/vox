package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Named
@AssignHomeworkChain({
        AH_CheckRequiredParameters.class,
        AH_CheckClazzGroup.class,
        AH_CheckUncheckedHomework.class,
        AH_ProcessHomeworkContent.class,
        AH_SaveHomework.class,
        AH_Callback.class
})
public class AssignHomeworkProcessor extends SpringContainerSupport {
    private final List<AbstractAssignHomeworkProcessor> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AssignHomeworkChain annotation = getClass().getAnnotation(AssignHomeworkChain.class);
        for (Class<? extends AbstractAssignHomeworkProcessor> beanClass : annotation.value()) {
            AbstractAssignHomeworkProcessor loader = getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException("Bean " + beanClass.getName() + " not found");
            }
            chains.add(loader);
        }
        logger.debug("Composed {} assign homework chains", chains.size());
    }

    public AssignHomeworkContext process(AssignHomeworkContext context) {
        if (context == null) return new AssignHomeworkContext().errorResponse();
        for (AbstractAssignHomeworkProcessor task : chains) {
            task.process(context);
            if (context.isTerminateTask()) {
                logger.debug("Task {} set terminateTask true, terminate", task.getClass().getName());
                break;
            }
        }
        return context.clearAdditions();
    }
}
