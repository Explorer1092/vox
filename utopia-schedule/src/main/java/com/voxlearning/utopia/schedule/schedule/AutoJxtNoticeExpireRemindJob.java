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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.vendor.consumer.VendorManagementClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2016/5/6
 */
@Named
@ScheduledJobDefinition(
        jobName = "家校通到期通知提醒",
        jobDescription = "每小时整点运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? "
)
public class AutoJxtNoticeExpireRemindJob extends ScheduledJobWithJournalSupport {

    @Inject
    private VendorManagementClient vendorManagementClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        vendorManagementClient.scheduleRemindExpireJxtNotice();
    }
}
