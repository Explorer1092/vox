
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.ChipsInvitionRewardLoader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语邀请提醒",
        jobDescription = "每天12:00执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 12 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoGenChipsInvitationNotifyJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @AlpsQueueProducer(queue = "utopia.chips.invitation.notify.message.queue")
    private MessageProducer chipsInvitationNotifyMessageProducer;

    @ImportService(interfaceClass = ChipsInvitionRewardLoader.class)
    private ChipsInvitionRewardLoader chipsInvitionRewardLoader;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Date endDate = Optional.ofNullable(chipsInvitionRewardLoader.loadInvitionConfig())
                .map(e -> SafeConverter.toString(e.get("acEndDate")))
                .map(DateUtils::stringToDate)
                .orElse(new Date());
        if (endDate.before(new Date())) {
            return;
        }

        Set<Long> userId = new HashSet<>();
        String sql = "SELECT DISTINCT `USER_ID` FROM `UCT_USER_WECHAT_REF` where TYPE = 3 AND DISABLED = b'0'";
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSql.withSql(sql).queryAll(((rs, rowNum) -> {
                    long uId = rs.getLong("USER_ID");
                    userId.add(uId);
                    return null;
                })))
                .execute();

        int index = 1;
        for (Long id : userId) {
            Map<String, Object> message = new HashMap<>();
            message.put("U", id);
            chipsInvitationNotifyMessageProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
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
