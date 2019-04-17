package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.campaign.api.TeacherActivityCardService;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "老师集卡片活动",
        jobDescription = "老师集卡片统计和添加库存",
        disabled = {Mode.STAGING, Mode.DEVELOPMENT, Mode.TEST},
        cronExpression = "0 5 0 * * ?",
        ENABLED = false
)
public class ActivityCardJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = TeacherActivityCardService.class)
    private TeacherActivityCardService teacherActivityCardService;
    @Inject
    private EmailServiceClient emailServiceClient;

    private static Date dayLimitStart; // 从12月31号开始每天放2个大字

    static {
        try {
            dayLimitStart = DateUtils.parseDate("2018-12-31", "yyyy-MM-dd");
        } catch (ParseException ignored) {
        }
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        try {
            sendMail();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (new Date().after(dayLimitStart)) {
            MapMessage result = teacherActivityCardService.setStock("1", 2);
            logger.info("每天添加大字库存 done {}", JSON.toJSONString(result));
        }
    }

    private void sendMail() {
        StringBuilder mailContent = new StringBuilder();

        MapMessage mapMessage = teacherActivityCardService.statisticsData();
        if (mapMessage.isSuccess()) {
            append(mailContent, mapMessage, "大");
            append(mailContent, mapMessage, "天");
            append(mailContent, mapMessage, "得");
            append(mailContent, mapMessage, "观");
            append(mailContent, mapMessage, "海");
            append(mailContent, mapMessage, "深");
            append(mailContent, mapMessage, "瞻");
            append(mailContent, mapMessage, "见");

            Long daStock = MapUtils.getLong(mapMessage, "daStock");
            Long giftStock = MapUtils.getLong(mapMessage, "giftStock");
            Long integral1Stock = MapUtils.getLong(mapMessage, "integral1Stock");
            Long integral2Stock = MapUtils.getLong(mapMessage, "integral2Stock");

            mailContent.append("\n");
            mailContent.append("大礼包剩余库存: ").append(giftStock).append("\n");
            mailContent.append("1园丁豆剩余库存: ").append(integral1Stock).append("\n");
            mailContent.append("2园丁豆剩余库存: ").append(integral2Stock).append("\n");

            emailServiceClient.createPlainEmail()
                    .body(mailContent.toString())
                    .subject("【" + RuntimeMode.current() + "】" + "寒假作业集卡活动")
                    .to(RuntimeMode.isProduction() ? "junbao.zhang@17zuoye.com;cong.yu@17zuoye.com;te.wang@17zuoye.com" : "junbao.zhang@17zuoye.com")
                    .send();
        }
    }

    public void append(StringBuilder stringBuilder, MapMessage data, String zi) {
        stringBuilder.append(zi)
                .append(" 字已发出: ")
                .append(MapUtils.getLong(data, zi))
                .append("\n");
    }
}
