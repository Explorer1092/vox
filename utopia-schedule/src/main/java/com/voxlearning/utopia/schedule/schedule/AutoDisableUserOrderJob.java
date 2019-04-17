package com.voxlearning.utopia.schedule.schedule;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "用户过期订单自动删除",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        ENABLED = false,
        cronExpression = "0 0 2 * * ? "
)
@ProgressTotalWork(100)
public class AutoDisableUserOrderJob extends ScheduledJobWithJournalSupport {

    @Inject UserOrderServiceClient userOrderServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        // 自动删除100天以前的未支付订单
        Date endDate = DateUtils.nextDay(new Date(), -100);
        long mod = 100;
        if (RuntimeMode.current().le(Mode.STAGING)) {
            mod = 2;
        }
        for (long i = 0; i < mod; i++) {
            userOrderServiceClient.disableUserOrder(i, endDate, OrderStatus.Canceled, PaymentStatus.Unpaid);
        }

    }
}
