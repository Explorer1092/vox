
package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
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

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * @Author songtao
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语微信模板修复",
        jobDescription = "每天7,8,9,10,11,19,20,21,22,23点执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 7,8,9,10,11,16,17,18,19,20,21,22,23 * * ?"
)
@ProgressTotalWork(100)
public class AutoFixWechatTemplateMessageSendJob extends ScheduledJobWithJournalSupport {

    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @AlpsQueueProducer(queue = "utopia.wechat.template.message.fix.send.queue")
    private MessageProducer wechatTemplateMessageFixSendProducer;

    private UtopiaSql utopiaSqlOrder;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("wechat");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        Set<Long> messageIds = new HashSet<>();
        List<Object> ids = (List<Object>) parameters.get("ids");
        if (CollectionUtils.isNotEmpty(ids)) {
            ids.forEach(e -> {
                messageIds.add(SafeConverter.toLong(e));
            });
            doSend(messageIds);
            return;
        }
        Date now = new Date();
        String date = DateUtils.dateToString(now);

        String sql = "SELECT DISTINCT ID FROM VOX_WECHAT_TEMPLATE_MESSAGE_RECORD WHERE  WECHAT_TYPE = 3 AND STATE = 'FAILED' AND CREATE_DATETIME > ?";
        RoutingPolicyExecutorBuilder.getInstance()
                .newExecutor()
                .policy(UtopiaRoutingDataSourcePolicy.UsingRandomSlave)
                .callback(() -> utopiaSqlOrder.withSql(sql).useParamsArgs(date).queryAll(((rs, rowNum) -> {
                    messageIds.add(rs.getLong("ID"));
                    return null;
                })))
                .execute();
        doSend(messageIds);
    }
    private void doSend(Collection<Long> messageIds) {
        if (CollectionUtils.isEmpty(messageIds)) {
            return;
        }
        int index = 1;
        for (Long id : messageIds) {
            Map<String, Object> message = new HashMap<>();
            message.put("ID", id);
            wechatTemplateMessageFixSendProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            if (index % 100 == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
            index++;
        }
    }
}
