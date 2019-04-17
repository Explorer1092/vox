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
 *
 * Created by yaguang.wang
 * on 2017/6/16.
 */
@Named
@ScheduledJobDefinition(
        jobName = "区分备忘录的类型",
        jobDescription = "区分备忘录的类型",
        disabled = {Mode.UNIT_TEST, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 10 * * ?",
        ENABLED = false
)
public class UpdateTeacherMemorandumJob extends ScheduledJobWithJournalSupport {
    @Inject private AgentCommandQueueProducer agentCommandQueueProducer;
    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Map<String, Object> command = new HashMap<>();
        command.put("command", "update_memorandum");
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
        progressMonitor.done();
    }
}
