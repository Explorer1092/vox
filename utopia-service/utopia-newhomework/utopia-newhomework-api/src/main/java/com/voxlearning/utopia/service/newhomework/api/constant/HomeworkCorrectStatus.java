package com.voxlearning.utopia.service.newhomework.api.constant;

/**
 * @author majianxin
 * @date 2018/5/25
 * @version V1.0
 */
public enum HomeworkCorrectStatus {

    WITHOUT_CORRECT,//无订正
    NOT_STARTED_CORRECT,//未订正
    CORRECT_FINISH;//已订正

    public static HomeworkCorrectStatus of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return WITHOUT_CORRECT;
        }
    }

}
