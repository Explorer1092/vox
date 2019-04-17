package com.voxlearning.utopia.service.business.impl.activity.listener;

import com.voxlearning.alps.core.calendar.StopWatch;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.enums.ActivityTypeEnum;
import com.voxlearning.utopia.service.business.impl.activity.entity.ReportContext;
import com.voxlearning.utopia.service.business.impl.activity.listener.handler.GenSudokuBaseData;
import com.voxlearning.utopia.service.business.impl.activity.listener.handler.GenTangramBaseData;
import com.voxlearning.utopia.service.business.impl.activity.listener.handler.GenTwoFourBaseData;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static com.voxlearning.utopia.service.business.impl.activity.listener.ActivityReportProducer.ACTIVITY_REPORT_EXPORT_TOPIC;

/**
 * 这个是为了单独导出数据的,可以幂等执行,不会损坏每日报表数据
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = ACTIVITY_REPORT_EXPORT_TOPIC),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = ACTIVITY_REPORT_EXPORT_TOPIC)
        },
        maxPermits = 2
)
public class ActivityReportExportListener implements MessageListener, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ActivityReportExportListener.class);

    @Inject
    private ActivityConfigServiceClient activityConfigServiceClient;
    @Inject
    private GenTangramBaseData genTangramBaseData;
    @Inject
    private GenTwoFourBaseData genTwoFourBaseData;
    @Inject
    private GenSudokuBaseData genSudokuBaseData;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void onMessage(Message message) {
        String activityId = message.getBodyAsString();
        StopWatch activityWatch = new StopWatch(true);
        logger.info("ActivityReportListener EXPORT activityId:{} start", activityId);
        try {
            ActivityConfig activityConfig = activityConfigServiceClient.getActivityConfigService().load(activityId);
            ReportContext reportContext = new ReportContext(activityConfig);
            reportContext.setWriteDatabase(false);
            if (activityConfig.getType() == ActivityTypeEnum.TANGRAM) {
                genTangramBaseData.execute(reportContext);
            } else if (activityConfig.getType() == ActivityTypeEnum.SUDOKU) {
                genSudokuBaseData.execute(reportContext);
            } else if (activityConfig.getType() == ActivityTypeEnum.TWENTY_FOUR) {
                genTwoFourBaseData.execute(reportContext);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            activityWatch.stop();
            long time = activityWatch.getTime(TimeUnit.SECONDS);
            logger.info("ActivityReportListener EXPORT activityId:{} end. time:{} seconds", activityId, time);
        }
    }

}
