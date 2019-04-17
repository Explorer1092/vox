package com.voxlearning.utopia.schedule.schedule.agent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
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
 * AgentDailyScoreStatisticsJob
 *
 * @author deliang.che
 * @since  2018/11/28
 */
@Named
@ScheduledJobDefinition(
        jobName = "日报得分统计任务",
        jobDescription = "日报得分统计任务",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 9 * * ?",
        ENABLED = true
)
public class AgentDailyScoreStatisticsJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Date startDate = null;
        Date endDate = null;
        if (parameters.containsKey("startDate")) {
            startDate = DateUtils.stringToDate(String.valueOf(parameters.get("startDate")), DateUtils.FORMAT_SQL_DATE);
        }

        if (parameters.containsKey("endDate")) {
            endDate = DateUtils.stringToDate(String.valueOf(parameters.get("endDate")), DateUtils.FORMAT_SQL_DATE);
        }

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "agent_daily_score_statistics");
        if(startDate != null){
            command.put("startDate", startDate.getTime());
        }
        if(endDate != null){
            command.put("endDate", endDate.getTime());
        }

        Message message = Message.newMessage();
        String json = JsonUtils.toJson(command);
        message.withPlainTextBody(json);
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }

}
