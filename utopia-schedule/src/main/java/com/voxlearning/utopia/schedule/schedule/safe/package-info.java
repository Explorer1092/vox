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

/**
 * 放在此包下的定时任务都是安全任务，可以在任意时间、任意运行模式下执行。
 */
@SafeScheduleJobs
package com.voxlearning.utopia.schedule.schedule.safe;

import com.voxlearning.alps.spi.schedule.SafeScheduleJobs;

class SafeScheduleConstants {
    /**
     * 安全定时任务的启动时间通常都是每天开始的时候。
     */
    static final String defaultCronExpression = "1 0 0 * * ?";
}