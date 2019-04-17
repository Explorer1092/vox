
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

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语邀请浏览提醒",
        jobDescription = "每天18:00执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 18 * * ?"
)
@ProgressTotalWork(100)
public class AutoGenChipsInvitationVisitNotifyJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @AlpsQueueProducer(queue = "utopia.chips.invitation.visit.notify.message.queue")
    private MessageProducer chipsInvitationVisityNotifyMessageProducer;

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
        Set<Long> userId = new HashSet<>();
        String date = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);
        String sql = "SELECT DISTINCT INVITER FROM VOX_CHIPS_ACTIVITY_INVITATION_VISIT WHERE CREATETIME >= '" + date + "'" ;
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSql.withSql(sql).queryAll(((rs, rowNum) -> {
                    long uId = rs.getLong("INVITER");
                    userId.add(uId);
                    return null;
                })))
                .execute();

        int index = 1;
        for (Long id : userId) {
            Map<String, Object> message = new HashMap<>();
            message.put("U", id);
            chipsInvitationVisityNotifyMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            if (index % 1000 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
            }
            index++;
        }
    }



}
