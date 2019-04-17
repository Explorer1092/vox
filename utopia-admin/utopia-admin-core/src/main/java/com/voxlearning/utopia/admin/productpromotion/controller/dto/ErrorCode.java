package com.voxlearning.utopia.admin.productpromotion.controller.dto;

import lombok.AllArgsConstructor;

/**
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-29 14:26
 **/
@AllArgsConstructor
public enum ErrorCode {

    UNKNOWN("999", "未处理的异常");
    public final String code;
    public final String desc;

}
