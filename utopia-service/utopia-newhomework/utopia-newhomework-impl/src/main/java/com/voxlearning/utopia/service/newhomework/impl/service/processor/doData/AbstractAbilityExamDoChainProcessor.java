package com.voxlearning.utopia.service.newhomework.impl.service.processor.doData;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.newhomework.impl.service.processor.AbilityExamSpringBean;

/**
 * @author lei.liu
 * @version 18-11-1
 */
public abstract class AbstractAbilityExamDoChainProcessor extends AbilityExamSpringBean {


    final public AbilityExamDoContext process(AbilityExamDoContext context) {
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

    abstract protected void doProcess(AbilityExamDoContext context);

}
