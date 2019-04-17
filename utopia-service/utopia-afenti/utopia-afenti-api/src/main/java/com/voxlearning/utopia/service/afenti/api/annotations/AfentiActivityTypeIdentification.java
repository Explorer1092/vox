package com.voxlearning.utopia.service.afenti.api.annotations;

import com.voxlearning.utopia.service.afenti.api.constant.AfentiActivityType;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @since 2016/8/15
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfentiActivityTypeIdentification {
    AfentiActivityType value();
}
