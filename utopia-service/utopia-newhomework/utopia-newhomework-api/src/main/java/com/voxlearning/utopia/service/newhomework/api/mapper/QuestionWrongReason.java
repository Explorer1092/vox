package com.voxlearning.utopia.service.newhomework.api.mapper;

import lombok.Getter;

/**
 * 错题订正用的错误原因
 *
 * @author xuesong.zhang
 * @since 2016-07-07
 */
@Deprecated
public enum QuestionWrongReason {
    Misread("没读懂题"),
    Mistake("计算错误"),
    Missing("读题马虎"),
    Other("其他原因");

    @Getter private final String desc;

    QuestionWrongReason(String desc) {
        this.desc = desc;
    }

    public static QuestionWrongReason of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return Other;
        }
    }
}
