package com.voxlearning.utopia.service.ai.constant;

public enum AIUnitMapType {
    SHOW_NORMAL("普通显示"),
    SHOW_EIGHT("当用户购买完旅行口语第5期后，显示 8 个课程"),
    ;
    private String desc;

    AIUnitMapType(String desc) {
        this.desc = desc;
    }
}
