package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author guoqiang.li
 * @since 2017/11/10
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BasicReviewContentType {
    WORD("单词表", "单词拼写、看图识词多维复习，整本教材","单词摸底","老师布置了期末单词表基础复习"),
    SENTENCE("重点句", "连词成句温习，整本教材","重点句摸底","老师布置了期末课文重点句复习"),
    CALCULATION("本学期计算能力回顾", "教材典型计算题练习，全面复习计算","计算能力","老师布置了期末计算能力复习"),
    READ_RECITE_WITH_SCORE("重点课文读背","重点课文，必背段落，温故而知新","课文读背","老师布置了重点课文读背内容");

    @Getter private final String name;
    @Getter private final String description;
    @Getter private final String jztTabName;
    @Getter private final String jztContent;

    public static BasicReviewContentType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }
}
