package com.voxlearning.washington.controller.thirdparty.qiyukf.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 七鱼客服分组定义
 *
 * @author Wenlong Meng
 * @version 1.0
 * @date 2018-08-28
 */
@AllArgsConstructor
public enum QiYuCustomerServiceGroup {
    YIQIXUE(711513, "一起学直播"),
    TEACHER_P(429247, "小学老师"),
    TEACHER_M(1302934, "中学老师"),
    STUDENT_P(425982, "小学生"),
    STUDENT_M(1302933, "中学生"),
    IVR(0, "IVR");

    @Getter
    int code;//分组id
    @Getter
    String desc;//描述

    /**
     * 查询分组id对应的客服分组，默认返回{@link #IVR}
     *
     * @param code
     * @return
     */
    public static QiYuCustomerServiceGroup of(int code){
        return Arrays.stream(QiYuCustomerServiceGroup.values()).filter(t -> t.code == code).findFirst().orElse(IVR);
    }

}
