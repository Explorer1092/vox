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

/**
 * 家长鼓励中的任务/目标状态
 *
 * @author RuiBao
 * @version 0.1
 * @since 1/12/2015
 */

public enum MissionState {
    WISH,
    ONGOING,
    COMPLETE;

    public static MissionState safeParse(String name) {
        return safeParse(name, null);
    }

    public static MissionState safeParse(String name, MissionState defaultValue) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
