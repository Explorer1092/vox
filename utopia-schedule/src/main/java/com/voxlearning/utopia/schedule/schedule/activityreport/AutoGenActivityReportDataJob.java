package com.voxlearning.utopia.schedule.schedule.activityreport;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateFormatUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.pubsub.ActivityReportProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import org.apache.http.client.utils.DateUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named
@ScheduledJobDefinition(
        jobName = "自动生成活动报告数据",
        jobDescription = "自动生成活动报告数据，每晚凌晨1点，跑批活动报告数据",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0 1 * * ?"
)
public class AutoGenActivityReportDataJob extends ScheduledJobWithJournalSupport {

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private ActivityReportProducer activityReportProducer;
    @Inject
    private EmailServiceClient emailServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<ActivityConfig> activityConfigs = activityConfigServiceClient.getActivityConfigService().loadAllActivityConfig();

        for (ActivityConfig activityConfig : activityConfigs) {
            activityReportProducer.getMessagePlainPublisher().publish(Message.newMessage().withPlainTextBody(activityConfig.getId()));
        }

        sendMail(activityConfigs);
    }

    private final String SPACE = "  ";

    private void sendMail(List<ActivityConfig> activityConfigs) {
        int sudokuCount = 0;
        int twentyFourCount = 0;
        int tangramCount = 0;

        List<ActivityConfig> teacherConfig = activityConfigs.stream().filter(ActivityConfig::hasTeacher).collect(Collectors.toList());
        for (ActivityConfig activityConfig : teacherConfig) {
            if (activityConfig.getType() == ActivityTypeEnum.TANGRAM) tangramCount++;
            if (activityConfig.getType() == ActivityTypeEnum.TWENTY_FOUR) twentyFourCount++;
            if (activityConfig.getType() == ActivityTypeEnum.SUDOKU) sudokuCount++;
        }

        StringBuilder teacherMsg = new StringBuilder();
        teacherMsg.append("老师端布置总数：").append(teacherConfig.size()).append(SPACE);
        teacherMsg.append("七巧板：").append(tangramCount).append(SPACE);
        teacherMsg.append("二十四点：").append(twentyFourCount).append(SPACE);
        teacherMsg.append("数独：").append(sudokuCount).append(SPACE);
        teacherMsg.append("\n");

        sudokuCount = 0;
        twentyFourCount = 0;
        tangramCount = 0;

        StringBuilder marketingDetailMsg = new StringBuilder();
        List<ActivityConfig> marketingConfig = activityConfigs.stream().filter(i -> !i.hasTeacher()).collect(Collectors.toList());
        for (ActivityConfig activityConfig : marketingConfig) {
            marketingDetailMsg.append("ID：").append(activityConfig.getId()).append(SPACE);
            marketingDetailMsg.append("类型：").append(activityConfig.getType().name()).append(SPACE);
            marketingDetailMsg.append("邮箱：").append(activityConfig.getEmail()).append(SPACE);
            marketingDetailMsg.append("标题：").append(activityConfig.getTitle()).append(SPACE);
            marketingDetailMsg.append("开始时间：").append(DateFormatUtils.format(activityConfig.getStartTime(), "yyyy-MM-dd")).append(SPACE);
            marketingDetailMsg.append("结束时间：").append(DateFormatUtils.format(activityConfig.getEndTime(), "yyyy-MM-dd")).append(SPACE);
            marketingDetailMsg.append("年级：").append(StringUtils.join(activityConfig.getClazzLevels(), ",")).append(SPACE);
            if (CollectionUtils.isNotEmpty(activityConfig.getAreaIds())) {
                marketingDetailMsg.append("投放区域数：").append(activityConfig.getAreaIds().size()).append(SPACE);
            }
            if (CollectionUtils.isNotEmpty(activityConfig.getSchoolIds())) {
                marketingDetailMsg.append("投放学校数：").append(activityConfig.getSchoolIds().size()).append(SPACE);
            }
            marketingDetailMsg.append("\n");

            if (activityConfig.getType() == ActivityTypeEnum.TANGRAM) tangramCount++;
            if (activityConfig.getType() == ActivityTypeEnum.TWENTY_FOUR) twentyFourCount++;
            if (activityConfig.getType() == ActivityTypeEnum.SUDOKU) sudokuCount++;
        }

        StringBuilder marketingMsg = new StringBuilder();
        marketingMsg.append("市场布置总数：").append(marketingConfig.size()).append(SPACE);
        marketingMsg.append("七巧板：").append(tangramCount).append(SPACE);
        marketingMsg.append("二十四点：").append(twentyFourCount).append(SPACE);
        marketingMsg.append("数独：").append(sudokuCount).append(SPACE);
        marketingMsg.append("\n\n\n");

        emailServiceClient.createPlainEmail()
                .to("junbao.zhang@17zuoye.com")
                .subject("【" + RuntimeMode.current() + "】 " + DateUtils.formatDate(new Date(), "yyyy-MM-dd") + " 今日活动汇总")
                .body(teacherMsg.append(marketingMsg).append(marketingDetailMsg).toString())
                .send();
    }

}

