package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Summer on 2019/1/16
 */

@Named
@ScheduledJobDefinition(
        jobName = "薯条英语自动退还学贝",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 */1 * * ? ",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoSendFinaceForChipsJob extends ScheduledJobWithJournalSupport {

    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private UserServiceClient userServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String userIds = SafeConverter.toString(parameters.get("userIds"));
        if (StringUtils.isBlank(userIds)) {
            logger.error("no userIds , param error.");
            return;
        }
        BigDecimal amount = Optional.ofNullable(parameters)
                .map(e -> e.get("amount"))
                .map(e -> SafeConverter.toDouble(e, 0.0D))
                .filter(e -> e > 0)
                .map(e -> new BigDecimal(e))
                .orElse(new BigDecimal(69.1));
        Set<String> userIdSet = new HashSet<>(Arrays.asList(StringUtils.split(userIds, ",")));
        for (String uid : userIdSet) {
            try {
                FinanceFlowContext context = FinanceFlowContext.instance()
                        .userId(SafeConverter.toLong(uid))
                        .type(FinanceFlowType.Deposit)
                        .state(FinanceFlowState.SUCCESS)
                        .amount(amount)
                        .payAmount(amount)
                        .payMethod("chips_manual")
                        .refer(FinanceFlowRefer.CRM);
                boolean result = financeServiceClient.getFinanceService()
                        .deposit(context).getUninterruptibly();
                if (!result) {
                    logger.error("chips add finance error, uid {}", uid);
                }
            } catch (Exception ex) {
                logger.error("chips add finance error, uid {}", uid, ex);
            }

            // 记录 UserServiceRecord
            UserServiceRecord userServiceRecord = new UserServiceRecord();
            userServiceRecord.setUserId(SafeConverter.toLong(uid));
            userServiceRecord.setOperatorId("xiaopeng.yang");
            userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
            userServiceRecord.setOperationContent("薯条英语差价退还学贝");
            userServiceRecord.setComments("薯条英语差价退还学贝" + amount.toString());
            userServiceClient.saveUserServiceRecord(userServiceRecord);
        }


    }
}
