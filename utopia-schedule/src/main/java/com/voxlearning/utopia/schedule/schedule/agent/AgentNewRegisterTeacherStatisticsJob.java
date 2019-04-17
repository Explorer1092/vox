package com.voxlearning.utopia.schedule.schedule.agent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AgentWorkRecordStatisticsJob
 *
 * @author song.wang
 * @date 2018/1/25
 */
@Named
@ScheduledJobDefinition(
        jobName = "天玑新注册老师跑数据  手动跑",
        jobDescription = "手动跑  不让自动跑",
        disabled = {Mode.TEST, Mode.STAGING, Mode.DEVELOPMENT,Mode.PRODUCTION},
        cronExpression = "0 30 6 * * ?",
        ENABLED = true
)
public class AgentNewRegisterTeacherStatisticsJob extends ScheduledJobWithJournalSupport {
    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        String dateStr = SafeConverter.toString(parameters.get("dateStr"),DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
        //天数
        Integer dayNum = SafeConverter.toInt(parameters.get("dayNum"),1);
        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "agent_register_teacher_statistics");
        command.put("dateStr", dateStr);
        command.put("dayNum", dayNum);

        Message message = Message.newMessage();
        String json = JsonUtils.toJson(command);
        message.withPlainTextBody(json);
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }
}
