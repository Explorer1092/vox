package com.voxlearning.ucenter.support.exception;

import com.voxlearning.ucenter.support.constants.SsoError;
import lombok.Getter;

/**
 * @author xinxin
 * @since 23/12/2015.
 */
public class SsoValidateException extends IllegalArgumentException {
    @Getter
    private SsoError error;

    public SsoValidateException(SsoError error) {
        super();

        this.error = error;
    }

}
