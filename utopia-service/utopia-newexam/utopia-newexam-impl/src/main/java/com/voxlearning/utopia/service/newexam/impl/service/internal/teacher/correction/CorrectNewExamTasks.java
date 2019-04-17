package com.voxlearning.utopia.service.newexam.impl.service.internal.teacher.correction;

import java.lang.annotation.*;

/**
 * Created by tanguohong on 2016/3/23.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CorrectNewExamTasks {
    Class<? extends CorrectNewExamTask>[] value();
}
