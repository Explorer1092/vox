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

package com.voxlearning.utopia.service.zone.impl.service.gift;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.runtime.RuntimeMode;

abstract class SendGiftConstants {
    static final DateRange springFestivalDateRange;

    static {
        if (RuntimeMode.lt(Mode.STAGING)) {
            springFestivalDateRange = new DateRange(
                    DateUtils.stringToDate("2015-02-10 00:00:00"),
                    DateUtils.stringToDate("2015-03-02 23:59:59"));
        } else {
            springFestivalDateRange = new DateRange(
                    DateUtils.stringToDate("2015-02-14 00:00:00"),
                    DateUtils.stringToDate("2015-03-02 23:59:59"));
        }
    }
}
