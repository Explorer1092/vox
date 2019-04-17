package com.voxlearning.utopia.service.ai.exception;


public class UnitConfigNotExitException extends Exception {

    public UnitConfigNotExitException(Throwable cause) {
        super(cause);
    }

    public UnitConfigNotExitException(String message) {
        super(message);
    }

    public UnitConfigNotExitException(String message, Throwable cause) {
        super(message, cause);
    }
}
