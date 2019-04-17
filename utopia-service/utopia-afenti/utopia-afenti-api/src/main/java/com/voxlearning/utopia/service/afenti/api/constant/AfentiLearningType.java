package com.voxlearning.utopia.service.afenti.api.constant;

/**
 * Created by Summer on 2017/7/5.
 */
public enum AfentiLearningType {
    castle,  // 阿芬提学习城堡
    preparation,  // 阿芬提预习
    review; //阿分题期末复习

    public static AfentiLearningType safeParse(String name) {
        try {
            return AfentiLearningType.valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }
}
