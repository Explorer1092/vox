package com.voxlearning.utopia.business.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BusinessErrorType {
    DEFAULT("100", "数据异常，重试一下吧"),
    NEED_LOGIN("101", "请重新登录"),
    PARAMETER_CHECK_ERROR("102", "参数异常"),
    DUPLICATED_OPERATION("103", "正在处理，请不要重复提交"),
    NO_CLAZZ("104", "没有班级信息");

    @Getter private final String code;
    @Getter private final String info;
}
