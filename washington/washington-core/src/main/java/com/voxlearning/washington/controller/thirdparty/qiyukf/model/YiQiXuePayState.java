package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 一起学直播付费状态
 *
 * @author Wenlong Meng
 * @version 1.0
 * @date 2018-08-29
 */
@AllArgsConstructor
public enum YiQiXuePayState {
    GUEST(-1, "未注册"),
    REGISTER(1, "注册"),
    PAID(5, "付费"),
    LEARNING(6, "学习"),
    BE_CONTINUED(7, "待续费");

    @Getter
    private int code;//编码
    @Getter
    private String desc;//描述

    /**
     * 查询编码对应的用户类型，默认返回{@link #GUEST}
     *
     * @param code
     * @return
     */
    public static YiQiXuePayState of(int code){
        return Arrays.stream(YiQiXuePayState.values()).filter(t -> t.code == code).findFirst().orElse(GUEST);
    }

}
