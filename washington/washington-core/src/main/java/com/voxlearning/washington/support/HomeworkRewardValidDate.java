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

package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.runtime.RuntimeMode;

import java.util.Date;

/**
 * Created by Sadi.Wan on 2014/6/3.
 */
public class HomeworkRewardValidDate {

    private static Date validDate;

    static {

        if (RuntimeMode.le(Mode.STAGING)) {
            validDate = DateUtils.stringToDate("2013-05-27 18:00:00");
        } else if (RuntimeMode.gt(Mode.STAGING)) {
            validDate = DateUtils.stringToDate("2014-06-09 00:00:00");
        } else {
            throw new RuntimeException("unknown RuntimeMode");
        }
    }

    public static boolean checkRewardValid(Date inDate) {
        return inDate.compareTo(validDate) >= 0;
    }

    public static Date getValidDate() {
        return validDate;
    }
}
