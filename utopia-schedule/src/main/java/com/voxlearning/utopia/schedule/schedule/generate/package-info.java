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
 * 自动产生数据写入缓存的定时任务。
 */
@GenerateScheduleJobs
package com.voxlearning.utopia.schedule.schedule.generate;

import com.voxlearning.alps.spi.schedule.GenerateScheduleJobs;

class GenerateScheduleConstants {
    /**
     * 自动产生数据任务的启动时间通常都是每天开始的时候。
     */
    static final String defaultCronExpression = "0 5 0 * * ?";
}