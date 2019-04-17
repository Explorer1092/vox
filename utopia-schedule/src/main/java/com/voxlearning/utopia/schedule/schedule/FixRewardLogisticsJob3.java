
package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Named
@Slf4j
@ScheduledJobDefinition(
        jobName = "补发收货人短信",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT, Mode.TEST},
        cronExpression = "0 0 6 1 * ?"
)
public class FixRewardLogisticsJob3 extends ScheduledJobWithJournalSupport {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql orderSqlReward;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private SmsServiceClient smsServiceClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        orderSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String sql = "SELECT DISTINCT RECEIVER_ID FROM VOX_REWARD_LOGISTICS WHERE CREATE_DATETIME >= ? AND TYPE = ? AND DISABLED = 0";

        List<Long> list = orderSqlReward.withSql(sql)
                .useParamsArgs(MonthRange.current().getStartDate(), RewardLogistics.Type.STUDENT.name())
                .queryAll((rs, rowNum) -> rs.getLong(1));

        for (Long teacherId : list) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);

            String unit = "50园丁豆";
            if (!teacherDetail.isPrimarySchool()) {
                unit = "500学豆";
            }

            sendSms(teacherId, String.format("亲爱的老师，您被选为#代收本校学生奖品#的幸运老师，如同意接收，学生快递发出后您将收到%s奖励，如您不愿代收，请务必今天之内联系400-160-1717反馈！", unit));
        }
    }

    private void sendSms(Long teacherId, String msg) {
        try {
            String mobile = sensitiveUserDataServiceClient.loadUserMobile(teacherId, "给收货老师发短信");
            if (StringUtils.isNotBlank(mobile)) {
                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setMobile(mobile);
                smsMessage.setType(SmsType.REWARD_NOTIFY.name());
                smsMessage.setSmsContent(msg);

                String time = DateUtils.dateToString(new Date(getDay0800()), "yyyyMMddHHmmss");
                smsMessage.setSendTime(time);
                smsServiceClient.getSmsService().sendSms(smsMessage);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private ZoneOffset zoneOffset = ZoneOffset.of("+8");

    private Long getDay0800() {
        LocalTime time = LocalTime.parse("08:00:00");
        LocalDateTime sendTime = LocalDate.now().atTime(time);
        return sendTime.toInstant(zoneOffset).getEpochSecond();
    }
}
