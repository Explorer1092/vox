package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

@Data
public class PsrMathPointPersistence {

    private Long pointId;
    private String pointName;

    // 调试使用
    public String formatToString() {
        return "(" + pointId.toString() + ":point#" + pointName + ")";
    }
}
