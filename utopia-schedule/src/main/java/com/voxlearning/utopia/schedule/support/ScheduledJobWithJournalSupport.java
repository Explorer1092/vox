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

package com.voxlearning.utopia.schedule.support;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.network.NetworkUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeApplication;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.utopia.schedule.journal.InternalJobJournalService;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;

import javax.inject.Inject;
import java.util.Map;

abstract public class ScheduledJobWithJournalSupport extends ProgressedScheduleJob {

    @Inject private InternalJobJournalService internalJobJournalService;

    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters,
                              ISimpleProgressMonitor progressMonitor) {

        String jobId = (String) parameters.get("RUNNING::JOB::ID");
        internalJobJournalService.jobStarted(
                jobId,
                startTimestamp,
                getJobName(),
                getClass().getName(),
                RuntimeApplication.getApplicationName(),
                NetworkUtils.getLocalHost(),
                NetworkUtils.getLocalHostname());

        boolean success = true;
        String errorMessage = null;
        try {
            executeScheduledJob(new InternalJobJournalLogger(jobId), startTimestamp, parameters, progressMonitor);
        } catch (Exception ex) {
            success = false;
            errorMessage = ex.getMessage();
            logger.error("Job '{}' failed: {}", getJobName(), ex.getMessage(), ex);
        } finally {
            long endTimestamp = System.currentTimeMillis();
            long duration = endTimestamp - startTimestamp;
            if (success) {
                internalJobJournalService.jobFinished(jobId, endTimestamp, duration, true, null);
            } else {
                internalJobJournalService.jobFinished(jobId, endTimestamp, duration, false, errorMessage);
            }
        }
    }

    // throw out exception if case of job failure

    abstract protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                                long startTimestamp,
                                                Map<String, Object> parameters,
                                                ISimpleProgressMonitor progressMonitor) throws Exception;

    private class InternalJobJournalLogger implements JobJournalLogger {
        private final String jobId;

        private InternalJobJournalLogger(String jobId) {
            this.jobId = jobId;
        }

        @Override
        public void log(String message, Object... arguments) {
            if (RuntimeMode.gt(Mode.STAGING)) return;
            if (StringUtils.isBlank(message)) return;
            message = StringUtils.formatMessage(message, arguments);
            internalJobJournalService.jobLogging(jobId, message);
        }
    }
}
