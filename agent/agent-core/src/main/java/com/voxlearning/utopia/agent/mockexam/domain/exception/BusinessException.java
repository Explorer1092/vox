package com.voxlearning.utopia.agent.mockexam.domain.exception;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.agent.mockexam.service.dto.ErrorCode;
import com.voxlearning.utopia.agent.mockexam.service.dto.Result;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author xiaolei.li
 * @version 2018/8/3
 */
public class BusinessException extends RuntimeException {

    @Getter
    private ErrorCode errorCode;

    public BusinessException(Result result) {
        super(result.getErrorMessage());
        String errorCode = result.getErrorCode();
        if (StringUtils.isNotBlank(errorCode))
            this.errorCode = ErrorCode.of(errorCode);
    }


    public BusinessException(ErrorCode errorCode) {
        super(errorCode.desc);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
