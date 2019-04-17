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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.BusinessManagementClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author changyuan.liu
 * @since 2015/3/19
 */
@Named
@ScheduledJobDefinition(
        jobName = "教研员学期行为数据统计任务",
        jobDescription = "教研员学期行为数据统计任务,每天2:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 30 4 * * ?",
        ENABLED = false
)
public class AutoResearchStaffBehaviorDataJob extends ScheduledJobWithJournalSupport {

    @Inject private BusinessManagementClient businessManagementClient;

    // 启动方法，用于测试
    public void startup(Map<String, Object> parameters) throws Exception {
//        executeScheduledJob(null, 0L, parameters);
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String endDateStr = parameters.get("endDate") != null ? parameters.get("endDate").toString() : null;
        String fromDateStr = parameters.get("fromDate") != null ? parameters.get("fromDate").toString() : null;
        String jobOnly = parameters.get("jobOnly") != null ? parameters.get("jobOnly").toString() : null;
        boolean needClear = SafeConverter.toBoolean(parameters.get("needClear"), false);

        businessManagementClient.scheduleAutoResearchStaffBehaviorDataJob(fromDateStr, endDateStr, jobOnly, needClear);
    }

}
