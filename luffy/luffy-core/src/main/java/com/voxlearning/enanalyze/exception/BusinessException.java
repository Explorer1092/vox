package com.voxlearning.enanalyze.exception;

import com.voxlearning.enanalyze.ViewCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public class BusinessException extends RuntimeException {

    /**
     * 试图错误码
     */
    @Getter
    private ViewCode viewCode;

    public BusinessException(ViewCode viewCode, String message) {
        super(message);
        this.viewCode = viewCode;
    }

    public BusinessException(ViewCode viewCode, String message, Throwable cause) {
        super(message, cause);
        this.viewCode = viewCode;
    }

    public BusinessException(ViewCode viewCode, Throwable cause) {
        super(cause);
        this.viewCode = viewCode;
    }
}
