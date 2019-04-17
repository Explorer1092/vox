package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 七鱼用户类型定义
 *
 * @author Wenlong Meng
 * @version 1.0
 * @date 2018-08-28
 */
@AllArgsConstructor
public enum QiYuUserType {
    PLATFORM(0, "平台"),
    AGENT(1, "市场"),
    YIQIXUE(2, "一起学直播");

    @Getter
    private int code;//编码
    @Getter
    private String desc;//描述

    /**
     * 查询编码对应的用户类型，默认返回{@link #PLATFORM}
     *
     * @param code
     * @return
     */
    public static QiYuUserType of(int code){
        return Arrays.stream(QiYuUserType.values()).filter(t -> t.code == code).findFirst().orElse(PLATFORM);
    }

}
