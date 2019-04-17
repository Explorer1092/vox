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
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.client.UserSmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatNotice;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2015/9/24.
 */
@Named
@ScheduledJobDefinition(
        jobName = "给家长推送前一天子女的消费总金额",
        jobDescription = "家长绑定了微信就发微信消息,否则尝试短信消息,每天上午9点运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 9 * * ?")
@ProgressTotalWork(100)
public class AutoSendWechatMessageOfOrderAmountJob extends ScheduledJobWithJournalSupport {

    @Inject private UserSmsServiceClient userSmsServiceClient;
    @Inject private ParentLoaderClient parentLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private WechatLoaderClient wechatLoaderClient;
    @Inject private WechatServiceClient wechatServiceClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;

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

        logger.info("AutoSendWechatMessageOfOrderAmountJob start");

        Date startDate = DayRange.current().previous().getStartDate();
        Date endDate = DayRange.current().previous().getEndDate();

        List<Map<String, Object>> allMapList = new ArrayList<>();
        int tableMod = 100;
        if (RuntimeMode.current().lt(Mode.STAGING)) {
            tableMod = 2;
        }
        for (int i = 0; i < tableMod; i++) {
            List<Map<String, Object>> mapList = userOrderLoaderClient.getUserPaidAmmount(SafeConverter.toLong(i), startDate, endDate);
            if (CollectionUtils.isNotEmpty(mapList)) {
                allMapList.addAll(mapList);
            }
        }

        if (allMapList.isEmpty()) {
            return;
        }

        progressMonitor.worked(20);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(80, allMapList.size());
        for (Map<String, Object> map : allMapList) {
            try {
                long studentId = ConversionUtils.toLong(map.get("USER_ID"));
                long amount = ConversionUtils.toLong(map.get("AMOUNT"));
                if (amount < 100) {
                    continue;
                }
                Student student = studentLoaderClient.loadStudent(studentId);
                if (student == null) {
                    continue;
                }

                String payload = student.fetchRealname() + "家长您好，您的孩子昨天在17作业开通学习产品共花费" + amount + "元，您可登录“家长通App”—“我的”—“我的订单”了解。";

                Long sendUserId;

                List<StudentParent> parents = parentLoaderClient.loadStudentParents(studentId);

                try {
                    Collections.sort(parents, (o1, o2) -> {
                        Date id1 = o1.getParentUser().getUpdateTime();
                        Date id2 = o2.getParentUser().getUpdateTime();
                        return id2.compareTo(id1);
                    });
                } catch (Exception e) {
                    logger.warn("Sorting error with data:{}", JsonUtils.toJson(parents), e);
                }

                StudentParent keyParent = parents.stream().filter(StudentParent::isKeyParent).findFirst().orElse(null);
                if (keyParent != null) {
                    sendUserId = keyParent.getParentUser().getId();
                } else if (CollectionUtils.isNotEmpty(parents)) {
                    sendUserId = parents.get(0).getParentUser().getId();
                } else {
                    sendUserId = studentId;
                }

                UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(sendUserId);
                if (userAuthentication != null && userAuthentication.isMobileAuthenticated()) {
                    userSmsServiceClient.buildSms().to(userAuthentication)
                            .content(payload)
                            .type(SmsType.AFENTI_AMOUNT_NOTICE)
                            .send();
                }

            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

}
