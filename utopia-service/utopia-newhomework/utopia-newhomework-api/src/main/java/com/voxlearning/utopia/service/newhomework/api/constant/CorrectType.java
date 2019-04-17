package com.voxlearning.utopia.service.newhomework.api.constant;


public enum CorrectType {
    SCORE,          // 主观打分
    CORRECT,        // 主观批改
    SCORE_CORRECT,  // 主观打分+主观批改
    UNKNOWN;        // 未知

    public static CorrectType of(String name) {
        try {
            return CorrectType.valueOf(name);
        } catch (Exception ex) {
            return UNKNOWN;
        }
    }
}