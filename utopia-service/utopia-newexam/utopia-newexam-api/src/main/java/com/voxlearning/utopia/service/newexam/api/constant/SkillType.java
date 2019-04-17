package com.voxlearning.utopia.service.newexam.api.constant;

import lombok.Getter;

public enum SkillType {
    SolvingArithmetic("运算能力"),
    SpatialImagination("空间观念"),
    DeductiveReasoning("推理能力");
    @Getter
    private final String desc;

    public static SkillType parse(String name) {
        try {
            return SkillType.valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }


    SkillType(String desc) {
        this.desc = desc;
    }
}
