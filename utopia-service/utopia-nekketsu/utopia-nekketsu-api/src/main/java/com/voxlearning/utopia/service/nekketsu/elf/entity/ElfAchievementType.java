/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.elf.entity;

import lombok.Getter;

/**
 * FIXME: =====================================================================
 * FIXME: 这个枚举严禁做任何修改！！！！
 * FIXME: 这个枚举严禁做任何修改！！！！
 * FIXME: 这个枚举严禁做任何修改！！！！
 * FIXME: 有人用了ordinal
 * FIXME: 有人用了ordinal
 * FIXME: 有人用了ordinal
 * FIXME: =====================================================================
 * Created by Sadi.Wan on 2015/2/26.
 */
@Getter
public enum ElfAchievementType {
    /*
     * FIXME: =================================================================
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: =================================================================
     */
    LOGIN_DAY(0, 5, 10, "累计进入英语动画绘本达到?天", null, new int[]{5, 10}),
    /*
     * FIXME: =================================================================
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: =================================================================
     */
    SUN_GET(0, 5, 20, "累计收集?个小太阳", null, new int[]{5, 10}),
    /*
     * FIXME: =================================================================
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: =================================================================
     */
    SAVE_PRINCE(1, 5, 0, "拯救精灵王子任务中拯救?", new String[]{"所有1星植物", "所有2星植物", "所有3星植物", "所有4星植物", "精灵王子"}, new int[]{5, 4, 4, 3, 1}),
    /*
     * FIXME: =================================================================
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: =================================================================
     */
    SAVE_QUEEN(1, 5, 0, "拯救精灵王后任务中拯救?", new String[]{"所有1星植物", "所有2星植物", "所有3星植物", "所有4星植物", "精灵王后"}, new int[]{5, 4, 4, 3, 1}),
    /*
     * FIXME: =================================================================
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: =================================================================
     */
    SAVE_KING(1, 5, 0, "拯救精灵王任务中拯救?", new String[]{"所有1星植物", "所有2星植物", "所有3星植物", "所有4星植物", "精灵王"}, new int[]{5, 4, 4, 3, 1}),
    /*
     * FIXME: =================================================================
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 这个枚举严禁做任何修改！！！！
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: 有人用了ordinal
     * FIXME: =================================================================
     */
    COMPOSE(0, 5, 20, "累计合成?个植物", null, new int[]{5, 10});

    private int pkCount;
    private int integralCount;
    private int inc;
    private String txtTemplate;
    private String[] incString;
    private int[] incInt;

    private ElfAchievementType(int pkCount, int integralCount, int inc, String txtTemplate, String[] incString, int[] incInt) {
        this.pkCount = pkCount;
        this.integralCount = integralCount;
        this.inc = inc;
        this.txtTemplate = txtTemplate;
        this.incString = incString;
        this.incInt = incInt;
    }

    public int calcFm(int stage) {
        if (stage < 0) {
            return 0;
        }
        if (stage < incInt.length) {//在incInt范围内
            if (0 == inc) {
                return incInt[stage];
            }
            if (0 == stage) {//对于每阶段独立的成就
                return incInt[stage];
            } else {//对于递增数量的成就
                return incInt[stage] - incInt[stage - 1];
            }
        }
        return inc;
    }

    /**
     * 获取每级成就起点基数
     *
     * @param stage
     * @return
     */
    public int getStageStart(int stage) {
        if (inc == 0) {//独立成就
            return 0;
        }

        if (0 == stage) {
            return 0;
        }
        if (stage < incInt.length) {
            return incInt[stage - 1];
        }

        int lastIncInt = incInt[incInt.length - 1];
        return lastIncInt + (stage - incInt.length) * inc;

    }

    public boolean isStageExchangable(int stage, int count) {
        if (stage < 0) {
            return false;
        }
        if (stage < incInt.length) {//在incInt范围内
            return incInt[stage] <= count;
        }
        if (inc == 0) {
            return false;
        }
        return incInt[incInt.length - 1] + (stage - incInt.length + 1) * inc <= count;
    }
}
