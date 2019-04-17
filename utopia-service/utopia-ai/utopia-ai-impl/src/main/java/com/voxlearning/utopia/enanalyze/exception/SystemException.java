package com.voxlearning.utopia.enanalyze.exception;

/**
 * 系统异常，无法预知的错误，不可控
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
