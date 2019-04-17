package com.voxlearning.utopia.service.newexam.impl.service.internal.student.result;

import java.lang.annotation.*;

/**
 * Created by tanguohong on 2016/3/10.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NewExamResultTasks {
    Class<? extends NewExamResultTask>[] value();
}
