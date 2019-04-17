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
        jobName = "天玑工作量统计",
        jobDescription = "天玑工作量统计",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "0 15 6 * * ?",
        ENABLED = true
)
public class AgentWorkRecordStatisticsJob extends ScheduledJobWithJournalSupport {
    @Inject
    private AgentCommandQueueProducer agentCommandQueueProducer;
    @Inject
    private ReportStatusServiceClient reportStatusServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);

        boolean judgeSummaryNew = true;
        if (parameters.containsKey("judgeSummaryNew")) {
            judgeSummaryNew = SafeConverter.toBoolean(parameters.get("judgeSummaryNew"), true);
        }
        if(judgeSummaryNew) {
            while (!isNewData()) {
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
        }

        Date startDate = null;
        Date endDate = null;
        if (parameters.containsKey("startDate")) {
            startDate = DateUtils.stringToDate(String.valueOf(parameters.get("startDate")), DateUtils.FORMAT_SQL_DATE);
        }

        if (parameters.containsKey("endDate")) {
            endDate = DateUtils.stringToDate(String.valueOf(parameters.get("endDate")), DateUtils.FORMAT_SQL_DATE);
        }

        Integer type = 1;  // 1:统计部门和人员的工作量  2：工作记录的T值计算
        if (parameters.containsKey("type")) {
            int typeTmp = SafeConverter.toInt(parameters.get("type"), 1);
            if(typeTmp == 1 || typeTmp == 2){
                type = typeTmp;
            }
        }


        // 发送命令让Agent去执行
        Map<String, Object> command = new HashMap<>();
        command.put("command", "agent_work_record_statistics");
        if(startDate != null){
            command.put("startDate", startDate.getTime());
        }
        if(endDate != null){
            command.put("endDate", endDate.getTime());
        }

        command.put("type", type);

        Message message = Message.newMessage();
        String json = JsonUtils.toJson(command);
        message.withPlainTextBody(json);
        agentCommandQueueProducer.getProducer().produce(message);

        progressMonitor.done();
    }

    // 判断数据是否已更新
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
