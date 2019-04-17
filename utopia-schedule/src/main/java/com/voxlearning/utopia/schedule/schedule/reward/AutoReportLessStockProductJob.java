package com.voxlearning.utopia.schedule.schedule.reward;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.entities.PlainEmail;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardSku;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 商品库存量预警
 * @author: kaibo.he
 * @create: 2018-12-11 16:21
 **/
@Named
@ScheduledJobDefinition(
        jobName = "商品库存量预警",
        jobDescription = "每周一早上6点跑一次",
        disabled = {Mode.DEVELOPMENT,Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 0 6 ? * MON"
)
public class AutoReportLessStockProductJob extends ScheduledJobWithJournalSupport {
    @Inject
    private RewardLoaderClient rewardLoaderClient;
    @Inject
    private NewRewardLoaderClient newRewardLoaderClient;
    @Inject
    private EmailServiceClient emailServiceClient;

    private static Integer reportStockLimit = 5;
    private static  String emailTo = "shan.wang@17zuoye.com;huishu.li@17zuoye.com;";
    private static  String testEmailTo = "shan.wang@17zuoye.com;huishu.li@17zuoye.com;kaibo.he@17zuoye.com;qiaoyun.liu@17zuoye.com;";

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        StringBuilder emailContent = new StringBuilder();
        Map<Long, RewardProduct> productMap = rewardLoaderClient.loadRewardProductMap();

        //检查实物
        Set<Long> productIds = productMap.values()
                .stream()
                .filter(product -> newRewardLoaderClient.isSHIWU(product.getOneLevelCategoryId()))
                .map(RewardProduct::getId)
                .collect(Collectors.toSet());
        Map<Long, List<RewardSku>> productRewardSkuMap = rewardLoaderClient.loadProductRewardSkus(productIds);
        productRewardSkuMap.values().stream().forEach(skus -> {
            List<RewardSku> srcSku = skus.stream().filter(sku -> sku.getInventorySellable() <= reportStockLimit).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(srcSku)) {
                RewardProduct product = productMap.get(srcSku.get(0).getProductId());
                if (Objects.nonNull(product) && product.getOnlined()) {
                    emailContent.append("商品:").append("《").append(product.getProductName()).append("》");
                    srcSku.stream().forEach(sku -> {
                        emailContent.append(",  sku:").append(sku.getSkuName()).append("库存数为：").append(sku.getInventorySellable());
                    });
                    emailContent.append("\r\n");
                }
            }
        });

        emailContent.append("\r\n");
        emailContent.append("\r\n");
        //检查优惠券
        Map<Long, Long> couponStork = newRewardLoaderClient.loadCouponStock();
        if (MapUtils.isNotEmpty(couponStork)) {
            for (Map.Entry<Long, Long> entry : couponStork.entrySet()) {
                if (entry.getValue() > reportStockLimit) {
                    continue;
                }
                RewardProduct product = productMap.get(entry.getKey());
                if (Objects.nonNull(product) && product.getOnlined()) {
                    emailContent.append("商品:").append("《").append(product.getProductName()).append("》").append("库存数为：").append(entry.getValue());
                    emailContent.append("\r\n");
                }
            }
        }
        if (StringUtils.isNotBlank(emailContent)) {
            PlainEmail plainEmail = new PlainEmail();
            plainEmail.setBody(emailContent.toString());
            if (!RuntimeMode.isProduction()) {
                plainEmail.setSubject("商品库存预警(测试)");
                plainEmail.setTo(testEmailTo);
            } else {
                plainEmail.setSubject("商品库存预警");
                plainEmail.setTo(emailTo);
            }
            emailServiceClient.getEmailService().sendEmail(plainEmail);
        }
    }

}
