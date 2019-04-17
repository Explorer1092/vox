/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rui.Bao
 * @since 2014-08-14 1:19 PM
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ClazzJournalCategory {

    MISC(0),            // 其他班级动态
    APPLICATION(1),     // 应用类班级动态
    LEARNING_CYCLE(2),  // 学习圈动态
    APPLICATION_STD(3);     //学生APP班级动态(也就是头条)

    @Getter private final int id;

    private static final Map<Integer, ClazzJournalCategory> map;

    static {
        map = Arrays.stream(values()).collect(Collectors.toMap(ClazzJournalCategory::getId, Function.identity()));
    }

    public static ClazzJournalCategory safeParse(Integer id) {
        return map.getOrDefault(id, MISC);
    }
}
