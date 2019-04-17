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

import com.voxlearning.utopia.service.action.api.support.AchievementType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Achievement implements Serializable {
    private static final long serialVersionUID = -9085310570939011443L;

    private static final String[] mapping = new String[]{
            "零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"
    };

    private String title;
    private int rank;
    private AchievementType type;

    public String toRank() {
        int i = Math.max(rank, 0);
        i = Math.min(i, 10);
        return mapping[i];
    }
}
