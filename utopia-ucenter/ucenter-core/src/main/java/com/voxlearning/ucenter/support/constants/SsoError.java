package com.voxlearning.ucenter.support.constants;

/**
 * @author xinxin
 * @since 23/12/2015.
 */
public enum SsoError {
    REQ_VALIDATE_INVALID_APP("01001", "invalid app"),  //应用不存在
    REQ_VALIDATE_INVALID_SIGN("01002", "invalid signature"), //签名验证失败
    REQ_VALIDATE_INVALID_PARAMTER_LIST("01003", "incorrect parameter list"), //参数缺失
    REQ_VALIDATE_INVALID_TIMESTAMP_EXPIRED("01004", "request timeout"); //时间截已过期

    private String code;
    private String msg;

    SsoError(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "{\"err\":\"" + code + "\",\"msg\":\"" + msg + "\"}";
    }
}
