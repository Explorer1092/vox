package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 七鱼用户类型排序
 *
 * @author Wenlong Meng
 * @version 1.0.0
 * @date 2018-08-28
 */
@AllArgsConstructor
public enum QiYuUserTypeSort {
    TEACHER(1),
    STUDENT(2),
    PARENT(3);

    @Getter
    private int sortValue;
}
