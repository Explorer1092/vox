package com.voxlearning.utopia.schedule.schedule.agent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
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

@Named
@ScheduledJobDefinition(
        jobName = "天玑活动数据统计任务",
        jobDescription = "天玑活动数据统计任务",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 0 2 * * ?",
        ENABLED = true
)
public class AgentCalActivityStatisticsDataJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        String activityId = "";
        Integer day = null;
        if(parameters.containsKey("activityId")){
            String aid = (String)parameters.get("activityId");
            if(StringUtils.isNotBlank(aid)){
                activityId = aid;
            }
        }

        if(parameters.containsKey("day")){
            day = SafeConverter.toInt(parameters.get("day"));
        }

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "agent_cal_activity_statistics");
        if(StringUtils.isNotBlank(activityId)){
            command.put("activityId", activityId);
        }
        if(day != null){
            command.put("day", day);
        }

        Message message = Message.newMessage();
        String json = JsonUtils.toJson(command);
        message.withPlainTextBody(json);
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }
}
