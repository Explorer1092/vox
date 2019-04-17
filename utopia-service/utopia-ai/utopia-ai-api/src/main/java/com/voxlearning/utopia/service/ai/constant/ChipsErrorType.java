package com.voxlearning.utopia.service.ai.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ChipsErrorType {
    NEED_LOGIN("440", "session失效"),
    SERVER_ERROR("500", "服务异常"),
    WECHAT_SERVER_ERROR("510", "微信服务异常"),
    SCORE_ERROR("520", "打分异常"),
    PARAMETER_ERROR("530", "参数异常"),
    DUPLICATED_OPERATION("540", "正在处理，请不要重复提交"),
    DEFAULT("400", "服务器错误");

    @Getter private final String code;
    @Getter private final String info;
}
