
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import lombok.Data;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语邀请未成功提醒",
        jobDescription = "每天10分钟执行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 */10 * * * ?"
)
@ProgressTotalWork(100)
public class AutoGenChipsInvitationNotPayedNotifyJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @AlpsQueueProducer(queue = "utopia.chips.invitation.not.payed.notify.message.queue")
    private MessageProducer chipsInvitationNotPayedMessageProducer;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_chipsenglish");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Invitation> userInvitation = new ArrayList<>();

        Date now = new Date();
        Date begin = DateUtils.addMinutes(now, -20);
        Date end = DateUtils.addMinutes(now, -10);
        String sql = "SELECT INVITER,INVITEE FROM VOX_CHIPS_ACTIVITY_INVITATION WHERE STATUS = 1 AND CREATETIME >= '" + DateUtils.dateToString(begin) + "'" + " AND CREATETIME < '" +  DateUtils.dateToString(end) + "'";
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSql.withSql(sql).queryAll(((rs, rowNum) -> {
                    long uId = rs.getLong("INVITER");
                    Invitation invitation = new Invitation();
                    invitation.setInviter(uId);
                    long invitee = rs.getLong("INVITEE");
                    invitation.setInvitee(invitee);
                    userInvitation.add(invitation);
                    return null;
                })))
                .execute();

        int index = 1;
        for (Invitation invitation : userInvitation) {
            Map<String, Object> message = new HashMap<>();
            message.put("U", invitation.getInviter());
            message.put("T", invitation.getInvitee());
            chipsInvitationNotPayedMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            if (index % 1000 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
            index++;
        }
    }


    @Data
    private class Invitation {
        private Long inviter;
        private Long invitee;
    }

}
