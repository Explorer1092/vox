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
 * Created by Summer Yang on 2015/7/24.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SchoolAmbassadorSource {
    NORMAL, COMPETITION, WECHAT;

    public static SchoolAmbassadorSource of(String name) {
        try {
            return SchoolAmbassadorSource.valueOf(name);
        } catch (Exception ignore) {
            return null;
        }
    }
}
