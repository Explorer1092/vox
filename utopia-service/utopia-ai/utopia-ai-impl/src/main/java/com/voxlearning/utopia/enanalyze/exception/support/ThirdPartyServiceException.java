package com.voxlearning.utopia.enanalyze.exception.support;

import com.voxlearning.utopia.enanalyze.exception.BusinessException;

/**
 * 第三方服务异常
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public class ThirdPartyServiceException extends BusinessException {
    public ThirdPartyServiceException(String message) {
        super(message);
    }

    public ThirdPartyServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThirdPartyServiceException(Throwable cause) {
        super(cause);
    }
}
