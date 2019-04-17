package com.voxlearning.utopia.schedule.schedule.agent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.common.ICharset;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.entity.crm.CrmClazzlevelSummary;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.AgentCommandQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * AutoAgentNewTeacherMessageJob
 *
 * @author song.wang
 * @date 2017/7/24
 */
@Named
@ScheduledJobDefinition(
        jobName = "新注册老师消息推送",
        jobDescription = "新注册老师消息推送",
        disabled = {Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 10 * * ?",
        ENABLED = true
)
public class AutoAgentNewTeacherMessageJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;
    @Inject
    private ReportStatusServiceClient reportStatusServiceClient;

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

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);

        while (!isNewData()){
            // 等待10分钟
            try {
                Thread.sleep(10 * 60000);
            } catch (InterruptedException e) {
            }
            // 如果超过了下午2点Summary数据还没有更新，那就退出，手工启动好了
            Date curTime = new Date();
            if (curTime.after(calendar.getTime())) {
                logger.warn("AutoAgentNewTeacherMessageJob stopped as the time is after 14:00");
                return;
            }
        }

        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "new_teacher_message");
        command.put("date", runningDay);

        Message message = Message.newMessage();
        String json = JsonUtils.toJson(command);
        message.withPlainTextBody(json);
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }

    // 判断summary数据是否已更新
    private boolean isNewData() {
        ReportStatus reportStatus = reportStatusServiceClient.getReportStatusService()
                .loadReportStatus()
                .getUninterruptibly();
        if (reportStatus == null) {
            return false;
        }
        Map<String, Object> latestCollection = reportStatus.getLatestCollection();
        if (latestCollection == null) {
            return false;
        }
        Map map = (Map) latestCollection.get("vox_teacher_summary");
        if (map == null) {
            return false;
        }
        String collectionName = (String) map.get("collection_name");
        Integer day = 0;
        if(StringUtils.isNotBlank(collectionName)){
            day = SafeConverter.toInt(collectionName.substring("vox_teacher_summary_".length()));
        }
        Integer lastDay = Integer.parseInt(DateUtils.dateToString(DateUtils.calculateDateDay(new Date(), -1), "yyyyMMdd"));
        return Objects.equals(day, lastDay);
    }
}
