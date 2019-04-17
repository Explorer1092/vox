/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-9-2
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GiftHistoryType {
    STUDENT_TO_TEACHER(1),
    STUDENT_TO_STUDENT(2),
    TEACHER_TO_STUDENT(3),
    TEACHER_TO_TEACHER(4);

    @Getter private final int id;
}
