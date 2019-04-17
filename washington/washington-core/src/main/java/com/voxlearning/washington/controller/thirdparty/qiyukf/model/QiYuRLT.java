package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 七鱼结果状态码
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-08-28
 */
@AllArgsConstructor
public enum QiYuRLT {

    SUCCESS(0, "成功"),
    FAILED(1, "失败"),
    EXPIRED(2, "失效");

    @Getter
    private int code;
    @Getter
    private String desc;
}
