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

package com.voxlearning.utopia.service.crm.provider.schedule;

import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.crm.impl.service.crm.ActivityConfigServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "每天凌晨0点1分增长进行中的活动 buffer version",
        cronExpression = "0 1 0 * * ? *"
)

public class IncrementOngoingActivityBufferVersionJob extends ProgressedScheduleJob {

    @Inject
    private ActivityConfigServiceImpl activityConfigService;

    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) {
        activityConfigService.incrementLatelyPassedActivityVersion();
        progressMonitor.done();
    }
}
