package com.voxlearning.washington.data.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 端标识
 *
 * @Author: wenlong meng
 * @since Jan 14, 2019
 */
public enum Terminal {

    teacher("17teacher", "小学老师"),
    student("17student","小学生"),
    parent("17parent","家长通"),
    juniorstu("17juniorstu", "中学生"),
    juniortea("17juniortea", "中学老师"),
    market("market", "天玑")
    ;

    @Getter
    public String desc;
    @Getter
    public String value;

    /**
     * 构建应用编码
     *
     * @param value
     * @param desc
     */
    Terminal(String value, String desc){
        this.desc = desc;
        this.value = value;
    }

    /**
     * 获取appCode
     *
     * @param value
     * @return
     */
    public static Terminal of(String value){
        return Arrays.stream(Terminal.values()).filter(f->f.value.equals(value)).findFirst().orElse(Terminal.student);
    }
}
