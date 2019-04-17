package com.voxlearning.utopia.service.newhomework.impl.service.processor.doData;

import com.voxlearning.utopia.service.newhomework.impl.service.processor.AbilityExamSpringBean;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Named
@AbilityExamDoChain({
        AbilityExamDo_InitData.class,
        AbilityExamDo_DoData.class
})
public class AbilityExamDoProcessor extends AbilityExamSpringBean {

    private final List<AbstractAbilityExamDoChainProcessor> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AbilityExamDoChain annotation = getClass().getAnnotation(AbilityExamDoChain.class);
        for (Class<? extends AbstractAbilityExamDoChainProcessor> beanClass : annotation.value()) {
            AbstractAbilityExamDoChainProcessor loader = getApplicationContext().getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException("Bean " + beanClass.getName() + " not found");
            }
            chains.add(loader);
        }
        logger.debug("Composed {} chains", chains.size());
    }

    public AbilityExamDoContext process(AbilityExamDoContext context) {
        if (context == null) return new AbilityExamDoContext().errorResponse();
        for (AbstractAbilityExamDoChainProcessor task : chains) {
            task.process(context);
            if (context.isTerminateTask()) {
                logger.debug("Task {} set / true, terminate", task.getClass().getName());
                break;
            }
        }
        return context.clearAdditions();
    }

}
