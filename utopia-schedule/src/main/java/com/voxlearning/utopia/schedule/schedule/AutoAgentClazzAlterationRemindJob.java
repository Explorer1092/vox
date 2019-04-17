package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
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
 * @author yuechen.wang
 * @date 2016/8/13
 */

@Named
@ScheduledJobDefinition(
        jobName = "市场处理换班申请提醒",
        jobDescription = "每天早上7点向市场人员推送T+1的换班申请处理提醒",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 7 * * ?"
)
@ProgressTotalWork(100)
public class AutoAgentClazzAlterationRemindJob extends ScheduledJobWithJournalSupport {

    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Date runDate = null;
        if (parameters.containsKey("date")) {
            runDate = DateUtils.stringToDate(String.valueOf(parameters.get("date")), DateUtils.FORMAT_SQL_DATE);
        }
        if (runDate == null) {
            runDate = new Date();
        }
        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "alteration_remind");
        command.put("date", runDate.getTime());
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }
}
