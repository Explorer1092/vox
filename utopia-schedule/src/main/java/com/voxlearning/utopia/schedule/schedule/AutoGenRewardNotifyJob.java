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
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.TeacherMessageType;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.reward.constant.OneLevelCategoryType;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


@Named
@ScheduledJobDefinition(
        jobName = "奖品中心每月月末25号检查用户实物兑换",
        jobDescription = "每个月25日06:00运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 6 25 * ?"
)
public class AutoGenRewardNotifyJob extends ScheduledJobWithJournalSupport {

    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    private UtopiaSql utopiaSqlOrder;
    private UtopiaSql utopiaSqlReward;
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSqlOrder = utopiaSqlFactory.getUtopiaSql("order");
        utopiaSqlReward = utopiaSqlFactory.getUtopiaSql("hs_reward");
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        boolean dryRun = SafeConverter.toBoolean(parameters.get("dryRun"));
        int month = MonthRange.current().getMonth();

        if(!RuntimeMode.isUsingTestData()){
            if (month == 12 || month == 1 || month == 7 || month == 6) {
                jobJournalLogger.log("寒暑假期间不执行。");
                return;
            }
        }

        Date startDate = MonthRange.current().getStartDate();
        if (month == 2 || month == 8) {
            startDate = MonthRange.current().previous().previous().getStartDate();
        }
        Date endDate = MonthRange.current().getEndDate();

        // 查询
        String sql = "SELECT BUYER_ID FROM VOX_REWARD_ORDER WHERE STATUS = ? AND CREATE_DATETIME >= ? " +
                "AND CREATE_DATETIME <= ? AND BUYER_TYPE = ? AND (PRODUCT_TYPE = ? OR PRODUCT_TYPE = ?) GROUP BY BUYER_ID HAVING SUM(TOTAL_PRICE) < 500";
        List<Long> userList = new ArrayList<>();
        utopiaSqlReward.withSql(sql).useParamsArgs(RewardOrderStatus.SUBMIT, startDate, endDate, UserType.TEACHER.getType(),
                RewardProductType.JPZX_SHIWU, OneLevelCategoryType.JPZX_SHIWU.intType().toString())
                .queryAll((rs, rowNum) -> {
            userList.add(rs.getLong("BUYER_ID"));
            return null;
        });

        if (dryRun) {
            logger.info("AutoGenRewardNotifyJob. user:{}", userList);
            return;
        }

        if (CollectionUtils.isNotEmpty(userList)) {
            Map<Long, TeacherDetail> teacherDetailMap = new HashMap<>();
            for (int i = 0; i < userList.size(); i += 200) {
                Map<Long, TeacherDetail> detailMap = teacherLoaderClient.loadTeacherDetails(userList.subList(i, Math.min(i + 200, userList.size())));
                if (MapUtils.isEmpty(detailMap)) {
                    continue;
                }
                teacherDetailMap.putAll(detailMap);
            }
            for (Long userId : userList) {
//                TeacherDetail teacherDetail = teacherDetailMap.get(userId);
//                boolean notify  = teacherDetail != null && grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail,"Reward",
//                        "ExchangeReduction", true);
//                if (!notify) {
//                    continue;
//                }

                try {
                    TeacherDetail teacherDetail = teacherDetailMap.get(userId);
                    String content = "您本月兑换的实物奖品还未达到包邮条件。本月最后一天24点前实物奖品未满500园丁豆，次月将扣除200园丁豆作为物流费用。";
                    if (teacherDetail.isJuniorTeacher()) {
                        content = "您本月兑换的实物奖品还未达到包邮条件。本月最后一天24点前实物奖品未满5000学豆，次月将扣除2000学豆作为物流费用。";
                    }
                    AppMessage message = new AppMessage();
                    message.setUserId(userId);
                    message.setMessageType(TeacherMessageType.ACTIVIY.getType());
                    message.setTitle("通知");
                    message.setContent(content);
                    messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
                    // 发pc端信息
                    teacherLoaderClient.sendTeacherMessage(userId, content);

                    appMessageServiceClient.sendAppJpushMessageByIds(content, AppMessageSource.PRIMARY_TEACHER, Arrays.asList(userId), new HashMap<>());
                } catch (Exception e) {
                    logger.error("send AppMessage error. userId:{}", userId, e);
                }
            }
        }
    }
}
