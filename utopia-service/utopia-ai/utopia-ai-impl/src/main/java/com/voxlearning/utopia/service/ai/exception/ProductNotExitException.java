package com.voxlearning.utopia.service.ai.exception;


public class ProductNotExitException extends Exception {

    public ProductNotExitException(Throwable cause) {
        super(cause);
    }

    public ProductNotExitException(String message) {
        super(message);
    }

    public ProductNotExitException(String message, Throwable cause) {
        super(message, cause);
    }
}
