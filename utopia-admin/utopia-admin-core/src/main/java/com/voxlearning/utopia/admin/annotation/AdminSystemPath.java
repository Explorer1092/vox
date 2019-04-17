package com.voxlearning.utopia.admin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Longlong Yu
 * @since 下午12:09,13-11-25.
 */
@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface AdminSystemPath {
    String value();
}
