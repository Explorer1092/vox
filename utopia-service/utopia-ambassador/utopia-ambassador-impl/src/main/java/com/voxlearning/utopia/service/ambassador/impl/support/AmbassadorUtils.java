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

package com.voxlearning.utopia.service.ambassador.impl.support;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;

public class AmbassadorUtils {

    // 根据不同的大使级别 获取对应的 图标统计时间
    public static DateRange getDateRange(AmbassadorSchoolRef ref, AmbassadorLevel level) {
        if (level == AmbassadorLevel.SHI_XI) {
            // 实习大使统计时间 是 大使的实习期
            return new DateRange(ref.getCreateDatetime(), DateUtils.calculateDateDay(ref.getCreateDatetime(), 30));
        } else {
            return MonthRange.current();
        }
    }
}
