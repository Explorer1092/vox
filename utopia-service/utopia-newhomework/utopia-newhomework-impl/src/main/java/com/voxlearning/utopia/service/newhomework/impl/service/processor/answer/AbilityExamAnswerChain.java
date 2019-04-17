package com.voxlearning.utopia.service.newhomework.impl.service.processor.answer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lei.liu
 * @version 18-11-1
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AbilityExamAnswerChain {

    Class<? extends AbstractAbilityExamAnswerChainProcessor>[] value();
}
