package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import java.lang.annotation.*;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HomeworkIndexDataChain {
    Class<? extends AbstractHomeworkIndexDataProcessor>[] value();
}
