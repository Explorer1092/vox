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

package com.voxlearning.utopia.api.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 家长鼓励中的任务/目标类型
 *
 * @author RuiBao
 * @version 0.1
 * @since 1/9/2015
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MissionType {
    OTHER, HomeworkType;

    public static MissionType of(String name) {
        try {
            return MissionType.valueOf(name);
        } catch (Exception ignored) {
            return null;
        }
    }
}
