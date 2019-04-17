package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author xin.xin
 * @since 2018/7/25
 **/
@Named
@ScheduledJobDefinition(
        jobName = "家庭等级下线兑换兴趣课任务",
        jobDescription = "家庭等级下线兑换兴趣课",
        disabled = {Mode.UNIT_TEST, Mode.PRODUCTION},
        cronExpression = "0 15 8 * * ?",
        ENABLED = false)
@ProgressTotalWork(100)
public class AutoHomeLevelShutDownJob extends ScheduledJobWithJournalSupport {
    @Inject
    private RewardServiceClient rewardServiceClient;
    @Inject
    private RewardLoaderClient rewardLoaderClient;
    @ImportService(interfaceClass = CRMRewardService.class)
    private CRMRewardService crmRewardService;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private UserIntegralServiceClient userIntegralServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Long productId = SafeConverter.toLong(parameters.get("productId"));
        if (0 == productId) {
            return;
        }
        String uids = SafeConverter.toString(parameters.get("uids"));
        if (StringUtils.isBlank(uids)) {
            return;
        }
        String[] userIds = uids.split(",");
        if (userIds.length == 0) {
            return;
        }

        RewardSku sku = rewardLoaderClient.loadProductSku(productId)
                .stream()
                .findFirst()
                .orElse(null);
        if (null == sku) {
            return;
        }

        for (String userId : userIds) {
            User user = userLoaderClient.loadUser(SafeConverter.toLong(userId));
            if (null == user) {
                continue;
            }

            RewardProductDetail rewardProductDetail = rewardLoaderClient.generateUserRewardProductDetail(user, productId);
            if (null == rewardProductDetail) {
                continue;
            }

            List<RewardOrder> rewardOrders = rewardLoaderClient.loadUserRewardOrders(user.getId());
            if (CollectionUtils.isNotEmpty(rewardOrders)) {
                boolean exists = rewardOrders.stream().anyMatch(order -> order.getProductId().equals(productId));
                if (exists) {
                    continue;
                }
            }

            IntegralHistory history = new IntegralHistory(user.getId(), IntegralType.其他, 10);
            history.setComment("家庭等级下线加学豆兑换奖品");

            userIntegralServiceClient.getUserIntegralService().changeIntegral(history);

            rewardServiceClient.createRewardOrder(user, rewardProductDetail, sku, 1, null, RewardOrder.Source.app);
        }
    }
}
