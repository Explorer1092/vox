package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.finish;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinishLiveCastHomeworkTasks {
    Class<? extends FinishLiveCastHomeworkTask>[] value();
}
