/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.api.constant;

import lombok.Getter;

/**
 * @author zhangbin
 * @since 2017/6/1 19:41
 */
public enum QuestionBoxType {
    READ(1, "课文朗读"),
    RECITE(2, "课文背诵");

    @Getter
    private Integer value;
    @Getter
    private String name;

    QuestionBoxType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static QuestionBoxType of(String name) {
        try {
            return QuestionBoxType.valueOf(name);
        } catch (Exception ignored) {
            return null;
        }
    }
}
