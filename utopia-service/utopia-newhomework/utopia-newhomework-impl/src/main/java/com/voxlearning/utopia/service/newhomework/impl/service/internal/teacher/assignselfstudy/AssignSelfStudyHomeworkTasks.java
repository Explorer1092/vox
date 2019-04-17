package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy;

import java.lang.annotation.*;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssignSelfStudyHomeworkTasks {
    Class<? extends AssignSelfStudyHomeworkTask>[] value();
}
