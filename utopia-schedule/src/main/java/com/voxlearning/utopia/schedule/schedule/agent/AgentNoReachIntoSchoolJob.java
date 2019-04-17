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
 * Created by yaguang.wang
 * on 2017/10/9.
 */
@Named
@ScheduledJobDefinition(
        jobName = "市场人员每日查询进校未达标的专员",
        jobDescription = "市场人员每日查询进校未达标的专员",
        disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING},
        cronExpression = "0 30 18 * * ?",
        ENABLED = true
)
public class AgentNoReachIntoSchoolJob  extends ScheduledJobWithJournalSupport {
    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Map<String, Object> command = new HashMap<>();
        command.put("command", "notice_region_early_warning");
        Message message = Message.newMessage();
        message.withStringBody(JsonUtils.toJson(command));
        agentCommandQueueProducer.getProducer().produce(message);
        progressMonitor.done();
    }
}
