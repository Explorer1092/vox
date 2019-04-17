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
 * @description:
 * @author: kaibo.he
 * @create: 2019-01-24 18:21
 **/
@Named
@ScheduledJobDefinition(
        jobName = "每日未评分字典表学校邮件任务",
        jobDescription = "每日未评分字典表学校邮件任务",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 2 * * ?",
        ENABLED = true
)
public class AgentEmailUnScoreDictSchoolJob extends ScheduledJobWithJournalSupport {
    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Date beginDate = null;
        Date endDate = null;
        if (parameters.containsKey("beginDate")) {
            beginDate = DateUtils.stringToDate(String.valueOf(parameters.get("beginDate")), DateUtils.FORMAT_SQL_DATETIME);
        }

        if (parameters.containsKey("endDate")) {
            endDate = DateUtils.stringToDate(String.valueOf(parameters.get("endDate")), DateUtils.FORMAT_SQL_DATETIME);
        }

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "agent_email_unscore_dict_school");
        if(beginDate != null){
            command.put("beginDate", beginDate.getTime());
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
