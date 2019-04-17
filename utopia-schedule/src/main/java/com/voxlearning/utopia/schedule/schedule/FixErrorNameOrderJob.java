package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 补发头饰的JOB
 * Created by haitian.gan on 2017/5/26.
 */
@Named
@ScheduledJobDefinition(
        jobName = "取消掉买了错误商品名字的订单",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT},
        ENABLED = false,
        cronExpression = "0 0 8 * * ? "
)
@ProgressTotalWork(100)
public class FixErrorNameOrderJob extends ScheduledJobWithJournalSupport {

    @Inject private RewardLoaderClient rewardLoaderClient;
    @Inject private RewardServiceClient rewardServiceClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        if(!parameters.containsKey("orderIds")){
            return;
        }

        String orderIdsStr = SafeConverter.toString(parameters.get("orderIds"),"");
        List<Long> orderIds = Arrays.stream(orderIdsStr.split(","))
                .map(SafeConverter::toLong)
                .collect(Collectors.toList());

        Map<Long,RewardOrder> orderMap = rewardLoaderClient.loadRewardOrders(orderIds);
        orderMap.forEach((id,order) -> {

            if(order.getDisabled())
                return;

            rewardServiceClient.deleteRewardOrder(order);
        });
    }
}
