package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign;

import java.lang.annotation.*;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/7
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssignHomeworkChain {
    Class<? extends AbstractAssignHomeworkProcessor>[] value();
}
