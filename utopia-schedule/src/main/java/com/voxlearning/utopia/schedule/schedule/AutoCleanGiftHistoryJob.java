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
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.queue.zone.ZoneEventType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.zone.client.ZoneQueueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer Yang on 2015/12/28.
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动清理礼物历史任务",
        jobDescription = "每天4点30运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 4 * * ?"
)
@ProgressTotalWork(100)
public class AutoCleanGiftHistoryJob extends ScheduledJobWithJournalSupport {

    @Inject private ZoneQueueServiceClient zoneQueueServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Map<String, Object> message = new HashMap<>();
        message.put("T", ZoneEventType.CLEANUP_GIFT_HISTORY);
        Message msg = Message.newMessage();
        msg.withStringBody(JsonUtils.toJson(message));
        zoneQueueServiceClient.getZoneQueueService().sendMessage(msg);

        jobJournalLogger.log("Cleanup all gift histories message sent to queue. Terminate");
        progressMonitor.done();
    }
}