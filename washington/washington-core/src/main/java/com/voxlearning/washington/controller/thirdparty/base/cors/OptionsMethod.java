package com.voxlearning.washington.controller.thirdparty.base.cors;

import java.lang.annotation.*;

/**
 * support {@link org.springframework.http.HttpMethod#OPTIONS}
 *
 * @author Wenlong Meng
 * @since Feb 1, 2019
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptionsMethod {
}
