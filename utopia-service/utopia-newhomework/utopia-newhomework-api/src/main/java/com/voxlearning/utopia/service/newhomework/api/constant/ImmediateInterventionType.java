package com.voxlearning.utopia.service.newhomework.api.constant;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 即时干预类型枚举
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ImmediateInterventionType {

    READING(1, "读题", Arrays.asList("再仔细读读题目，也许就有新发现", "再<span class=\"txtBlue\">仔细读</span>一下题目试试")),
    REASONING(2, "推理列式", Collections.singletonList("再<span class=\"txtBlue\">细心做</span>一次试试")),
    OPERATION(3, "计算画图", Arrays.asList("不小心算错了吗？再算一次吧", "再<span class=\"txtBlue\">仔细算</span>一次试试")),
    TYPING(4, "输入", Arrays.asList("写错了吗？再检查一下书写吧", "再检查一遍<span class=\"txtBlue\">书写</span>试试")),
    WORDTEACH(5, "", Arrays.asList(""));    //字词讲练专用

    @Getter private final Integer hintId; // 提示ID
    @Getter private final String description;
    @Getter private final List<String> hintDesc; //提示描述

    public static ImmediateInterventionType create(Integer hintId) {
        for (ImmediateInterventionType immediateInterventionType : values()) {
            if (immediateInterventionType.hintId.equals(hintId)) {
                return immediateInterventionType;
            }
        }
        return null;
    }

}


