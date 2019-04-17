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
 * AutoAgentWeeklyJob
 *
 * @author song.wang
 * @date 2016/8/13
 */

@Named
@ScheduledJobDefinition(
        jobName = "市场周报生成",
        jobDescription = "生成市场人员（专员，市经理）的周报",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 11 ? * SUN")
@ProgressTotalWork(100)
public class AutoAgentWeeklyJob extends ScheduledJobWithJournalSupport {

    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

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
        command.put("command", "generate_weekly");
        command.put("date", runningDay);
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }
}
