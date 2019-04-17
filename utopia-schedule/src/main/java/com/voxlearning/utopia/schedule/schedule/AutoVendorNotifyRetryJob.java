/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.email.api.constants.EmailTemplate;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.VendorConstants;
import com.voxlearning.utopia.service.vendor.api.constant.VendorNotifyChannel;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;
import com.voxlearning.utopia.service.vendor.consumer.VendorManagementClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 通过MQ发送失败的消息通过此任务重新发送。
 * Created by Alex on 2014/11/11.
 */
@Named
@ScheduledJobDefinition(
        jobName = "投递失败通知消息自动重发",
        jobDescription = "自动重发给第三方APP投递失败的通知消息，每30分钟运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 */5 * * * ? "
        //ENABLED = false
)
@ProgressTotalWork(100)
public class AutoVendorNotifyRetryJob extends ScheduledJobWithJournalSupport {

    @Inject private EmailServiceClient emailServiceClient;

    @Inject private VendorManagementClient vendorManagementClient;
    @Inject private VendorServiceClient vendorServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        // 每次执行取出数据库中需要重新投递的消息，最大1000条进行投递
        // 重新投递对象:
        //     STATUS:0                     未投递成功
        //     RETRY_COUNT:>=0 AND <4        最大重新投递三次
        List<VendorNotify> notifyList = vendorManagementClient.findUndeliveriedNotify();
        // 为了防止重复通知，对于retry_count=0的，要判断一下update_datetime 7 mintues 以上
        Long curTime = System.currentTimeMillis();

        notifyList = notifyList.stream()
                .filter(p -> p.getRetryCount() > 0 || (curTime - p.getUpdateDatetime().getTime()) > 420000)
                .collect(Collectors.toList());

        if (notifyList != null && notifyList.size() > 0) {
            for (VendorNotify notify : notifyList) {
                // 按照情况做一下延时重试, 最大重试3次，按照延迟5M,25M，125M处理
                if (notify.getRetryCount() > 0) {
                    Date updateTime = notify.getUpdateDatetime();
                    Double shouldWatiTime = Math.pow(5, notify.getRetryCount()) * 60 * 1000;
                    if ((curTime - updateTime.getTime()) < shouldWatiTime) {
                        continue;
                    }
                }

                Long notifyId = notify.getId();
                String appKey = notify.getAppKey();
                String targetUrl = notify.getTargetUrl();
                String content = notify.getNotify();

                if (VendorConstants.JPUSH_STUDENT_APP_KEY.equals(appKey)) {
                    //捋的一遍，应该不会有这个 appKey。为了把发 push 拆到 push-provider 里，要把这个vendorManagementClient.getVendorPushConfigurationAuthentication 从 vendor里干掉
//                    vendorServiceClient.createVendorNotify(String.valueOf(notifyId))
//                            .channel(VendorNotifyChannel.JPUSH)
//                            .appKey(appKey)
//                            .targetUrl(targetUrl)
//                            .auth(vendorManagementClient.getVendorPushConfigurationAuthentication("17Student"))
//                            .jsonContent(content)
//                            .send();
                } else {
                    Map<String, Object> params = JsonUtils.fromJson(content);
                    vendorServiceClient.createVendorNotify(String.valueOf(notifyId))
                            .channel(VendorNotifyChannel.HTTP)
                            .appKey(appKey)
                            .targetUrl(targetUrl)
                            .params(params)
                            .send();
                }
            }
        }

        // 看一下数据库里面是否有当日超过重试次数的通知
        // 只在线上做
        if (RuntimeMode.isProduction()) {
            List<VendorNotify> deliveryFailedList = vendorManagementClient.findTodayDeliveryFailedNotify();
            if (deliveryFailedList != null && deliveryFailedList.size() > 0) {
                Map<String, Object> content = new HashMap<>();

                Set<Long> failedIds = new HashSet<>();
                deliveryFailedList.forEach(p -> failedIds.add(p.getId()));

                content.put("info", "今日有给第三方APP发送失败的通知，数量:" + deliveryFailedList.size() + ", ids:" + failedIds.toString());
                emailServiceClient.createTemplateEmail(EmailTemplate.office)
                        .to("zhilong.hu@17zuoye.com")
                        .cc("zhilong.hu@17zuoye.com")
                        .subject("给第三方APP发送通知失败")
                        .content(content)
                        .send();
            }
        }

        progressMonitor.done();
    }

}
