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

package com.voxlearning.utopia.schedule.journal;

import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.logger.LoggerFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

@Named
public class InternalJobJournalService {
    private static final Logger logger = LoggerFactory.getLogger(InternalJobJournalService.class);

    @Inject private JobJournalDao jobJournalDao;

    public void jobStarted(String jobId,
                           long jobStartTime,
                           String jobName,
                           String jobClass,
                           String jobApplication,
                           String jobIpAddresses,
                           String jobHostname) {
        DayRange jobDay = DayRange.newInstance(jobStartTime);
        String jobStartDate = jobDay.toString();

        JobJournal jobJournal = new JobJournal();
        jobJournal.setId(jobId);
        jobJournal.setJobName(jobName);
        jobJournal.setJobClass(jobClass);
        jobJournal.setJobApplication(jobApplication);
        jobJournal.setJobIpAddresses(jobIpAddresses);
        jobJournal.setJobHostname(jobHostname);
        jobJournal.setJobStartDate(jobStartDate);
        jobJournal.setJobStartTime(new Date(jobStartTime));
        try {
            jobJournalDao.insert(jobJournal);
        } catch (Exception ex) {
            logger.warn("Failed to save JobJournal [jobName={},jobClass={}]",
                    jobName, jobClass, ex);
        }
    }

    public void jobFinished(String jobId,
                            long jobEndTime,
                            Long duration,
                            boolean success,
                            String errorMessage) {
        try {
            jobJournalDao.finish(jobId, jobEndTime, duration, success, errorMessage);
        } catch (Exception ex) {
            logger.warn("Failed to update JobJournal '{}'", jobId, ex);
        }
    }

    public void jobLogging(String jobId, String logMessage) {
        try {
            jobJournalDao.logging(jobId, logMessage);
        } catch (Exception ignored) {
        }
    }
}
