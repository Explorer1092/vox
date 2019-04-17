package com.voxlearning.utopia.enanalyze.mq;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 主题类型
 *
 * @author xiaolei.li
 * @version 2018/7/27
 */
@AllArgsConstructor
public enum Topic {
    ARTICLE_CREATE("作文 - 新建"),
    ARTICLE_UPDATE("作文 - 修改"),
    ARTICLE_DELETE("作文 - 删除"),
    UNKNOWN("未知类型");

    public final String desc;

    public static Topic of(String name) {
        return Arrays.stream(Topic.values())
                .filter(i -> i.name().equals(name))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
