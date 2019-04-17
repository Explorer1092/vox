package com.voxlearning.utopia.enanalyze;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 错误码
 */
@AllArgsConstructor
public enum ErrorCode {

    WX_LOGIN_ERROR("550", "微信登录时发生错误"),
    AI_OCR_ERROR("560", "ai的ocr接口出现错误"),
    AI_NLP_ERROR("570", "ai的nlp接口出现错误"),
    DAO_RS_EMPTY("580", "查询数据库结果集为空"),
    DAO_EXE_ERROR("581", "数据库执行出错"),
    BIZ_ERROR("500", "业务异常"),
    UNKNOWN("999", "未处理的业务异常"),;

    public final String CODE;
    public final String DESC;

    public static ErrorCode of(String code) {
        return Arrays.stream(ErrorCode.values())
                .filter(i -> i.CODE.equals(code))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
