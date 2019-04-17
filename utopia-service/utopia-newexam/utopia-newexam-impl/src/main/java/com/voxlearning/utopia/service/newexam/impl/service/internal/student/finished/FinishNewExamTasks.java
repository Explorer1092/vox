package com.voxlearning.utopia.service.newexam.impl.service.internal.student.finished;

import java.lang.annotation.*;

/**
 * Created by tanguohong on 2016/3/11.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FinishNewExamTasks {
    Class<? extends FinishNewExamTask>[] value();
}
