package com.voxlearning.luffy.exception;

import com.voxlearning.alps.remote.hydra.exception.ExecutionException;
import lombok.Getter;


public class MiniProgramErrorException extends ExecutionException {
    private static final long serialVersionUID = 5716568527125585244L;

    @Getter
    private String code;
    @Getter
    private String message;


    public MiniProgramErrorException(String code,String message) {
        super(String.format("Error code: %s , case: %s", code, message));
        this.code=code;
        this.message = message;
    }
}
