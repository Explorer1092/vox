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

package com.voxlearning.utopia.service.action.api.document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 效率，硬编码这里。修改成长等级配置时，先弄明白了
 * http://wiki.17zuoye.net/pages/viewpage.action?pageId=25592381
 */
public class UserGrowthLevel {

    private static final int[] levelMapping;
    private static final String[] titleMapping;

    static {
        // 原始配置
        // 左边是成长值的范围，右边是对应等级
        LinkedHashMap<IntRange, Integer> original = new LinkedHashMap<>();
        original.put(new IntRange(0, 10 - 1), 1);
        original.put(new IntRange(10, 25 - 1), 2);
        original.put(new IntRange(25, 45 - 1), 3);
        original.put(new IntRange(45, 70 - 1), 4);
        original.put(new IntRange(70, 100 - 1), 5);
        original.put(new IntRange(100, 135 - 1), 6);
        original.put(new IntRange(135, 175 - 1), 7);
        original.put(new IntRange(175, 220 - 1), 8);
        original.put(new IntRange(220, 270 - 1), 9);
        original.put(new IntRange(270, 330 - 1), 10);
        original.put(new IntRange(330, 400 - 1), 11);
        original.put(new IntRange(400, 480 - 1), 12);
        original.put(new IntRange(480, 570 - 1), 13);
        original.put(new IntRange(570, 670 - 1), 14);
        original.put(new IntRange(670, 780 - 1), 15);
        original.put(new IntRange(780, 900 - 1), 16);
        original.put(new IntRange(900, 1030 - 1), 17);
        original.put(new IntRange(1030, 1170 - 1), 18);
        original.put(new IntRange(1170, 1320 - 1), 19);
        original.put(new IntRange(1320, 1480 - 1), 20);
        original.put(new IntRange(1480, 1650 - 1), 21);
        original.put(new IntRange(1650, 1830 - 1), 22);
        original.put(new IntRange(1830, 2020 - 1), 23);
        original.put(new IntRange(2020, 2220 - 1), 24);
        original.put(new IntRange(2220, 2430 - 1), 25);
        original.put(new IntRange(2430, 2650 - 1), 26);
        original.put(new IntRange(2650, 2880 - 1), 27);
        original.put(new IntRange(2880, 3120 - 1), 28);
        original.put(new IntRange(3120, 3370 - 1), 29);
        // 大于等于3370，目前就是30级



        int[] a = new int[3371];
        for (Map.Entry<IntRange, Integer> entry : original.entrySet()) {
            IntRange r = entry.getKey();
            int level = entry.getValue();
            for (int i = r.getMinimum(); i <= r.getMaximum(); i++) {
                a[i] = level;

            }
        }
        a[3370] = 30; // 30级封顶
        levelMapping = a;

        //左边是成长等级范围,右边是称号
        LinkedHashMap<IntRange, String> originalTitle = new LinkedHashMap<>();
        originalTitle.put(new IntRange(1, 5), "初级种子");
        originalTitle.put(new IntRange(6, 10), "中级种子");
        originalTitle.put(new IntRange(11, 15), "高级种子");
        originalTitle.put(new IntRange(16, 20), "初级豆苗");
        originalTitle.put(new IntRange(21, 25), "中级豆苗");
        originalTitle.put(new IntRange(26, 30), "高级豆苗");

        String[] b = new String[31];
        for (Map.Entry<IntRange, String> entry : originalTitle.entrySet()) {
            IntRange r = entry.getKey();
            String title = entry.getValue();
            for (int i = r.getMinimum(); i <= r.getMaximum(); i++) {
                b[i] = title;
            }
        }
        titleMapping = b;
    }

    // 范围内的等级，直接用下标可以取到
    public static int getLevel(int value) {
        if (value < 0) {
            // 照理说，这个不应该发生
            return 1;
        }
        if (value >= 3370) {
            // 大于等于3370，目前就是30级
            return 30;
        }
        return levelMapping[value];
    }

    //计算升级所需的成长值
    public static int valueNeededForLevelUp(int value) {
        int level = getLevel(value);
        if (level == 30) {
            return 0;
        }

        for (int i = value + 1; i < levelMapping.length; i++) {
            if (levelMapping[i] != level) {
                return i - value;
            }
        }

        return 0;
    }

    //等级的称号,直接用下标可以取到
    public static String getTitle(int value) {
        if (value < 0) {
            return null;
        }

        if (value > 30) {
            return null;
        }

        return titleMapping[value];
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class IntRange {
        private final int minimum;
        private final int maximum;
    }
}
