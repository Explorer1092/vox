package com.voxlearning.utopia.service.newhomework.impl.service.processor.answer;

import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.AbilityExamSpringBean;

import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Named
@AbilityExamAnswerChain({
        AbilityExamAnswer_InitContextData.class,        // 初始化+检查数据
        AbilityExamAnswer_CalculateScore.class,         // 计算题目是否正确
        AbilityExamAnswer_SaveQuestionAnswer.class      // 保存做题结果
})
public class AbilityExamAnswerProcessor extends AbilityExamSpringBean {

    private final List<AbstractAbilityExamAnswerChainProcessor> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        AbilityExamAnswerChain annotation = getClass().getAnnotation(AbilityExamAnswerChain.class);
        for (Class<? extends AbstractAbilityExamAnswerChainProcessor> beanClass : annotation.value()) {
            AbstractAbilityExamAnswerChainProcessor loader = getApplicationContext().getBean(beanClass);
            if (loader == null) {
                throw new IllegalStateException("Bean " + beanClass.getName() + " not found");
            }
            chains.add(loader);
        }
        logger.debug("Composed {} chains", chains.size());
    }

    public AbilityExamAnswerContext process(AbilityExamAnswerContext context) {
        if (context == null) return new AbilityExamAnswerContext().errorResponse();
        for (AbstractAbilityExamAnswerChainProcessor task : chains) {
            task.process(context);
            if (context.isTerminateTask()) {
                logger.debug("Task {} set terminateTask true, terminate", task.getClass().getName());
                break;
            }
        }
        return context.clearAdditions();
    }

}
