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
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "市场排行榜",
        jobDescription = "市场排行榜",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 15 10 * * ?"
)
@ProgressTotalWork(100)
public class AutoAgentPerformanceRankingJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        int runningDay = 0;
        if (parameters.containsKey("date")) {
            Date runningDate = DateUtils.stringToDate(String.valueOf(parameters.get("date")), DateUtils.FORMAT_SQL_DATE);
            if (runningDate == null) {
                logger.error("invalid running date format: {}", parameters.get("date"));
                return;
            }
            // 运行日期
            runningDay = SafeConverter.toInt(DateUtils.dateToString(runningDate, "yyyyMMdd"));
        }

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "performance_ranking");
        command.put("date", runningDay);
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
        progressMonitor.done();
    }
}
