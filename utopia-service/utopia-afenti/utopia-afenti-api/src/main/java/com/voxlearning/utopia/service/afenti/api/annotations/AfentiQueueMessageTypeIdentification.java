package com.voxlearning.utopia.service.afenti.api.annotations;

import com.voxlearning.utopia.service.afenti.api.constant.AfentiQueueMessageType;

import java.lang.annotation.*;

/**
 * @author Ruib
 * @since 2016/7/25
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfentiQueueMessageTypeIdentification {
    AfentiQueueMessageType value();
}
