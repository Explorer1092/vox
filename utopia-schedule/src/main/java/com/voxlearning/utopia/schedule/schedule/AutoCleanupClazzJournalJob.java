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
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.zone.api.constant.ZoneConstants;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "删除班级动态任务",
//        jobDescription = "每周一运行一次，从数据库中删除两周前的班级动态",
        jobDescription = "每天运行一次，从数据库中删除14天之前的班级动态",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 45 4 ? * *")
@ProgressTotalWork(100)
public class AutoCleanupClazzJournalJob extends ScheduledJobWithJournalSupport {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        jobJournalLogger.log("Current date is '{}'", new Date(startTimestamp));

//        Date timestamp = ZoneConstants.getClazzJournalStartDate();
        Date timestamp = DateUtils.calculateDateDay(new Date(startTimestamp), -14);
        jobJournalLogger.log("Delete all clazz journals before '{}'", timestamp);

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("T", ZoneEventType.CLEANUP_CLAZZ_JOURNAL);
        message.put("TS", timestamp.getTime());
        Message msg = Message.newMessage();
        msg.withStringBody(JsonUtils.toJson(message));
        zoneQueueServiceClient.getZoneQueueService().sendMessage(msg);

        jobJournalLogger.log("Cleanup clazz journal message sent to queue. Terminate");
        progressMonitor.done();
    }

}
