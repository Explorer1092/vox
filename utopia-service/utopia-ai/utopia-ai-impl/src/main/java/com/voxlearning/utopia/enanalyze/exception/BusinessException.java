package com.voxlearning.utopia.enanalyze.exception;

/**
 * 业务异常，可识别的异常，在捕获时可以提取栈顶message用于错误提示
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public class BusinessException extends RuntimeException {

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
