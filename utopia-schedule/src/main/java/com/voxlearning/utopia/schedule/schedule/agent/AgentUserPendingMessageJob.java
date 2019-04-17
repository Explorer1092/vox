package com.voxlearning.utopia.schedule.schedule.agent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户待审批消息汇总任务
 *
 * @author chunlin.yu
 * @create 2017-07-31 15:14
 **/
@Named
@ScheduledJobDefinition(
        jobName = "市场人员待我审核的内容数量消息推送",
        jobDescription = "市场人员待我审核的内容数量消息推送",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 9 * * ?",
        ENABLED = true
)
public class AgentUserPendingMessageJob extends ScheduledJobWithJournalSupport {
    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Map<String, Object> command = new HashMap<>();
        command.put("command", "agent_user_pending_count_message");
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
        progressMonitor.done();
    }
}
