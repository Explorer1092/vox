package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.calculator;

import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/20
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireObjectiveConfigTypes {
    ObjectiveConfigType[] value();
}
