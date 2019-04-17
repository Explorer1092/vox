package com.voxlearning.enanalyze;

import com.voxlearning.utopia.enanalyze.ErrorCode;
import lombok.AllArgsConstructor;

/**
 * 视图错误代码
 *
 * @author xiaolei.li
 * @version 2018/7/6
 */
@AllArgsConstructor
public enum ViewCode {
    BIZ_ERROR("500", "业务异常"),
    BIZ_ILLEGAL_ARGUMENT("510", "业务参数异常"),
    AI_ERROR("550", "ai服务异常"),
    SESSION_INVALID("9000", "会话失效"),
    UNKNOWN("9999", "未知异常");
    final public String CODE;
    final public String DESC;

    public static ViewCode of(int code) {
        for (ViewCode i : ViewCode.values()) {
            if (i.CODE.equals(code))
                return i;
        }
        return UNKNOWN;
    }

    /**
     * 错误码映射
     */
    abstract static class Mapper {
        public static ViewCode of(ErrorCode errorCode) {
            ViewCode viewCode;
            switch (errorCode) {
                case WX_LOGIN_ERROR: {
                    viewCode = SESSION_INVALID;
                    break;
                }
                case AI_OCR_ERROR:
                case AI_NLP_ERROR: {
                    viewCode = AI_ERROR;
                    break;
                }
                case BIZ_ERROR:
                case DAO_RS_EMPTY:
                case DAO_EXE_ERROR: {
                    viewCode = BIZ_ERROR;
                    break;
                }
                default: {
                    viewCode = UNKNOWN;
                }
            }
            return viewCode;
        }
    }
}
