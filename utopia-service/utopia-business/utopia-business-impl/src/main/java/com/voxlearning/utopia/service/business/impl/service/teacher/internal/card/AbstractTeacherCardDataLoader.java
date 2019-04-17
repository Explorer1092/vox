package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.monitor.FlightRecorder;

/**
 * Created by tanguohong on 2017/4/17.
 */
abstract public class AbstractTeacherCardDataLoader extends SpringContainerSupport {

    final public TeacherCardDataContext process(TeacherCardDataContext context) {
        if (context == null) {
            return null;
        }
        FlightRecorder.dot(getClass().getSimpleName());
        try {
            return doProcess(context);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return context;
        }
    }

    abstract protected TeacherCardDataContext doProcess(TeacherCardDataContext context);
}
