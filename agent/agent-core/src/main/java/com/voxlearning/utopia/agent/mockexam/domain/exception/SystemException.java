package com.voxlearning.utopia.agent.mockexam.domain.exception;

/**
 * 系统异常
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
public class SystemException extends RuntimeException {
    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }
}
