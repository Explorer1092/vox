package com.voxlearning.utopia.service.newhomework.api.exception;

/**
 * @author steven
 * @since 2017/1/23
 */
public class CannotCreateHomeworkException extends RuntimeException {

    private static final long serialVersionUID = 7246237311222584128L;

    public CannotCreateHomeworkException(Throwable cause) {
        super(cause);
    }

    public CannotCreateHomeworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotCreateHomeworkException(String message) {
        super(message);
    }

    public CannotCreateHomeworkException() {
    }
}
