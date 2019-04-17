package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy.finish;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2017/2/6
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinishSelfStudyHomeworkTasks {
    Class<? extends FinishSelfStudyHomeworkTask>[] value();
}
