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

/**
 * AutoAgentNeedFollowUpJob
 *
 * @author song.wang
 * @date 2016/7/29
 */
@Named
@ScheduledJobDefinition(
        jobName = "市场需要跟进的学校及老师任务",
        jobDescription = "市场需要跟进的学校及老师任务（Agent首页线索用）",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 15 8 * * ?")
@ProgressTotalWork(100)
public class AutoAgentNeedFollowUpJob extends ScheduledJobWithJournalSupport {

    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Date runningDate = new Date();
        if (parameters.containsKey("date")) {
            runningDate = DateUtils.stringToDate(String.valueOf(parameters.get("date")), DateUtils.FORMAT_SQL_DATE);
            if (runningDate == null) {
                logger.error("invalid running date format: {}", parameters.get("date"));
                return;
            }
        }

        // 运行日期
        Integer runningDay = SafeConverter.toInt(DateUtils.dateToString(runningDate, "yyyyMMdd"));

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "need_follow_up");
        command.put("date", runningDay);
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }
}
