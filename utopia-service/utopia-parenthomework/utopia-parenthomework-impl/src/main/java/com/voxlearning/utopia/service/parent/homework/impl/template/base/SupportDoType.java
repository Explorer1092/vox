package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import com.voxlearning.utopia.service.parent.homework.api.model.DoType;

import java.lang.annotation.*;

/**
 * 支持类型
 *
 * @author Wenlong Meng
 * @since Mar 21, 2019
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportDoType {
    DoType value() default DoType.DO;
}
