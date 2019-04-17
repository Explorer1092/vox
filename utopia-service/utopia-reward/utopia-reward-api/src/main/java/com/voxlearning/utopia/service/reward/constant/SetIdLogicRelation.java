package com.voxlearning.utopia.service.reward.constant;

import lombok.Getter;

/**
 * @description: 对应库中分类集合的id...
 * @author: kaibo.he
 * @create: 2018-11-07 20:07
 **/
public enum SetIdLogicRelation {
    TEACHER_PRIVILEGE(Integer.valueOf(1), "特权专区"),
    TEACHER_PUBLIC_GOOD(Integer.valueOf(2), "公益专区"),
    TEACHER_YIQI(Integer.valueOf(3), "一起专属"),
    TEACHER_HOT(Integer.valueOf(4), "大家都喜欢"),
    TEACHER_PRIVILEGE_SENIOR(Integer.valueOf(5), "特权专区"),
            ;
    @Getter
    private Integer number;
    @Getter
    private String name;
    SetIdLogicRelation(Integer number, String name) {
        this.name = name;
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }

}
