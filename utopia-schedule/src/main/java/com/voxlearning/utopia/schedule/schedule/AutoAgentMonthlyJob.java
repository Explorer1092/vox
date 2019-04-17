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
 * AutoAgentMonthlyJob
 *
 * @author song.wang
 * @date 2016/10/26
 */
@Named
@ScheduledJobDefinition(
        jobName = "市场月报生成",
        jobDescription = "生成市场人员（专员，市经理, 大区经理）的月报（每月1号执行）",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.PRODUCTION},  // 由于月初可能要进行人员组织结构调整，另外工资计算可能需要进行多次调整，为保持月报执行结构与工资数据保持一致， 决定月报的运行调整为手动运行，运行的时机在工资计算之后，人员组织结构调整之前运行
        cronExpression = "0 0 11 1 * ?")
@ProgressTotalWork(100)
public class AutoAgentMonthlyJob extends ScheduledJobWithJournalSupport {

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
        command.put("command", "generate_monthly");
        command.put("date", runningDay);
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }
}
