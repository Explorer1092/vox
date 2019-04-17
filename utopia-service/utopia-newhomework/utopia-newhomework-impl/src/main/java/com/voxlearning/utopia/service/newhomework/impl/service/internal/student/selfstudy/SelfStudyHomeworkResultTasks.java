package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.selfstudy;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2017/2/3
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelfStudyHomeworkResultTasks {
    Class<? extends SelfStudyHomeworkResultTask>[] value();
}
