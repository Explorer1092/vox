/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.afenti.api.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Afenti user level definitions.
 *
 * @author Guohong Tan
 * @since 2013-09-14 13:19
 */

/* =================================================
 * 注意
 *
 * 如果没有特殊的需求，不需要给枚举加个什么INT类型的值。
 * 这种做法就是多余。枚举本身就是为了解决这类问题而产生的。
 *
 * 枚举的名字一旦确定，未来即便发现错误也请尽量不要修改。
 * 尽量不要删除已有的枚举定义。
 *
 * (1) 如果需要增加新的枚举，请随意。
 * (2) 尽量不要修改和删除现有的枚举，如果发现有错误，可以
 *     添加新的枚举值，将错误的标记为@Deprecated
 * ================================================= */

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AfentiUserLevel {
    A(6, "A"),
    B(5, "B"),
    C(4, "C"),
    D(3, "D"),
    E(2, "E"),
    F(1, "F");

    @Getter private final int level;
    @Getter private final String description;

    public static final AfentiUserLevel defaultLevel;
    public static final int maxLevel;
    public static final int minLevel;
    private static final Map<Integer, AfentiUserLevel> afentiUserLevels;

    static {
        defaultLevel = D;
        afentiUserLevels = new LinkedHashMap<>();

        TreeSet<Integer> levels = new TreeSet<>();
        for (AfentiUserLevel afentiUserLevel : values()) {
            levels.add(afentiUserLevel.getLevel());
            afentiUserLevels.put(afentiUserLevel.level, afentiUserLevel);
        }
        minLevel = levels.first();
        maxLevel = levels.last();
    }

    public static AfentiUserLevel of(Integer level) {
        if (afentiUserLevels.containsKey(level)) {
            return afentiUserLevels.get(level);
        }
        return defaultLevel;
    }
}
