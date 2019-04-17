package com.voxlearning.utopia.service.business.impl.service.teacher.internal.card;

import java.lang.annotation.*;

/**
 * Created by tanguohong on 2017/4/17.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TeacherCardDataChain {
    Class<? extends AbstractTeacherCardDataLoader>[] value();
}
