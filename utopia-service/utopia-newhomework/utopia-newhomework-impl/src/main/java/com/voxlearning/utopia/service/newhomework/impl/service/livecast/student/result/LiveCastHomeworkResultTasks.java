package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LiveCastHomeworkResultTasks {
    Class<? extends LiveCastHomeworkResultTask>[] value();
}
