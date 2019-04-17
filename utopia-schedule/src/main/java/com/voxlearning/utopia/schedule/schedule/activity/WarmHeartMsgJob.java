package com.voxlearning.utopia.schedule.schedule.activity;

import com.google.common.util.concurrent.RateLimiter;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@SuppressWarnings("ALL")
@ScheduledJobDefinition(
        jobName = "亲子计划发运营消息",
        jobDescription = "亲子计划发运营消息",
        disabled = {Mode.STAGING, Mode.TEST, Mode.DEVELOPMENT},
        cronExpression = "0 0 2 * * ?"
)
public class WarmHeartMsgJob extends ScheduledJobWithJournalSupport {

    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    private UtopiaSql utopiaSql;

    @AlpsPubsubPublisher(topic = "utopia.campaign.warm_heart.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePlainPublisher;

    @Override
    public void afterPropertiesSet() throws Exception {
        utopiaSql = utopiaSqlFactory.getUtopiaSql("hs_misc");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        RateLimiter rateLimiter = RateLimiter.create(1); // 每秒查1次,每次10000条

        long lastId = 0L;

        while (true) {
            List<Long> userList = fetchMoreUserList(lastId, 1000);

            if (CollectionUtils.isEmpty(userList)) {
                jobEnd();
                return;
            }

            for (Long userId : userList) {
                lastId = userId;

                try {
                    Message message = Message.newMessage().withPlainTextBody(userId.toString());
                    messagePlainPublisher.publish(message);
                } catch (Exception e) {
                    if (RuntimeMode.isUsingTestData()) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            rateLimiter.acquire();
        }


    }

    private void jobEnd() {
        emailServiceClient.createPlainEmail()
                .body("正常结束")
                .subject("【" + RuntimeMode.current() + "】" + "亲子计划发运营消息")
                .to("junbao.zhang@17zuoye.com")
                .send();
    }

    private List<Long> fetchMoreUserList(Long userId, Integer pageSize) {
        return utopiaSql.withSql("SELECT ID FROM `VOX_WARM_HEART_PLAN_ACTIVITY` WHERE ID > " + userId + " LIMIT " + pageSize)
                .queryAll((rs, rowNum) -> rs.getLong("ID"));
    }
}
