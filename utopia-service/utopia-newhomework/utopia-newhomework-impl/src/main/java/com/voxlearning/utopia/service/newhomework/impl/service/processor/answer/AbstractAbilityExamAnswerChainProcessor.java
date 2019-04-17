package com.voxlearning.utopia.service.newhomework.impl.service.processor.answer;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.newhomework.api.context.bonus.AbilityExamAnswerContext;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.AbilityExamSpringBean;

/**
 * @author lei.liu
 * @version 18-11-1
 */
public abstract class AbstractAbilityExamAnswerChainProcessor extends AbilityExamSpringBean {

    final public AbilityExamAnswerContext process(AbilityExamAnswerContext context) {
        if (context == null) {
            return null;
        }
        FlightRecorder.dot(getClass().getSimpleName());
        try {
            doProcess(context);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return context;
        }
        return context;
    }

    abstract protected void doProcess(AbilityExamAnswerContext context);

}
