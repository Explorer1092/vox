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

package com.voxlearning.utopia.service.afenti.api;

import lombok.Getter;

/**
 * 错题的状态类型
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
public enum AfentiWrongQuestionStateType {
    incorrect("incorrect"),
    similar("similar"),
    rescued("rescued");
    @Getter
    private String code;

    AfentiWrongQuestionStateType(String code) {
        this.code = code;
    }

    public static AfentiWrongQuestionStateType safeParse(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
