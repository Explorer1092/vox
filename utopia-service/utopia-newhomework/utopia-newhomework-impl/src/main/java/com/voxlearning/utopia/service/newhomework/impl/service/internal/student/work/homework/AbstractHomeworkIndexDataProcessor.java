package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
abstract public class AbstractHomeworkIndexDataProcessor extends NewHomeworkSpringBean {

    final public HomeworkIndexDataContext process(HomeworkIndexDataContext context){
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

    abstract protected void doProcess(HomeworkIndexDataContext context);
}
