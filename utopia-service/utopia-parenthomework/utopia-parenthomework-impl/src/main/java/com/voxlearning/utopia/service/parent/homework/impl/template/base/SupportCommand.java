package com.voxlearning.utopia.service.parent.homework.impl.template.base;

import com.voxlearning.utopia.service.parent.homework.api.model.Command;

import java.lang.annotation.*;

/**
 * 支持操作
 *
 * @author Wenlong Meng
 * @since Feb 20, 2019
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportCommand {
    Command value();//操作
}
