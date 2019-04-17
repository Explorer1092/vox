package com.voxlearning.utopia.admin.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author xuerui.zhang
 * @since 2019/1/17 上午11:04
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RecommendationFiledNameType {

    FILED_1("轮播课程id"),
    FILED_2("轮播开始时间"),
    FILED_3("轮播结束时间"),
    FILED_4("轮播权重值"),
    FILED_5("是否在适龄模块展示");

    private final String name;
}
