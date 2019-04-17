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

import com.voxlearning.alps.lang.calendar.WeekRange;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

abstract public class ZoneConstants {

    /**
     * 签到的费用
     */
    public static final int COST_SIGN_IN = 5;

    /**
     * 表情评论
     */
    public static final List<Long> IMG_COMMENT = Collections.unmodifiableList(
            Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L)
    );

    /**
     * 班级新鲜事的有效声明周期是2个星期
     * 包含本周，向前推2周。这里计算起始的时间点，供查询时使用
     *
     * @return 班级新鲜事的有效起始时间
     */
    public static Date getClazzJournalStartDate() {
        return WeekRange.current().previous().previous().getStartDate();
    }
}
