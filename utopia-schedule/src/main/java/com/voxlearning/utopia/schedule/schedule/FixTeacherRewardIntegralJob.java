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
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


@Named
@ScheduledJobDefinition(
        jobName = "奖品中心修复误扣的老师园丁豆",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.PRODUCTION},
        cronExpression = "0 0 5 1 * ?",
        ENABLED = false
)
public class FixTeacherRewardIntegralJob extends ScheduledJobWithJournalSupport {

    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private GrayFunctionManagerClient grayFunctionManagerClient;
    @ImportService(interfaceClass = UserIntegralService.class)
    private UserIntegralService userIntegralService;

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
        Date startDate = MonthRange.current().previous().getStartDate();
        Date endDate = MonthRange.current().previous().getEndDate();
        // 查询
        String sql = "SELECT BUYER_ID FROM VOX_REWARD_ORDER WHERE BUYER_TYPE = 1 AND CREATE_DATETIME >= ? AND CREATE_DATETIME <= ? AND PRODUCT_TYPE = 'JPZX_SHIWU' AND DISABLED = '0'  AND BUYER_ID NOT IN(" +
                "  SELECT BUYER_ID FROM VOX_REWARD_ORDER WHERE CREATE_DATETIME >= ? AND CREATE_DATETIME <= ? AND PRODUCT_TYPE = 'JPZX_SHIWU' AND DISABLED = '0' AND BUYER_ID LIKE '1%' GROUP BY BUYER_ID HAVING SUM(TOTAL_PRICE) < 500" +
                ") GROUP BY BUYER_ID HAVING SUM(TOTAL_PRICE) < 500";
        List<Long> userList = new ArrayList<>();
        utopiaSqlReward.withSql(sql).useParamsArgs(startDate, endDate, startDate, endDate).queryAll((rs, rowNum) -> {
            userList.add(rs.getLong("BUYER_ID"));
            return null;
        });

        if (CollectionUtils.isEmpty(userList)) {
            return;
        }
        Map<Long, TeacherDetail> teacherDetailMap = new HashMap<>();
        for (int i = 0; i < userList.size(); i += 200) {
            Map<Long, TeacherDetail> detailMap = teacherLoaderClient.loadTeacherDetails(userList.subList(i, Math.min(i + 200, userList.size())));
            if (MapUtils.isEmpty(detailMap)) {
                continue;
            }
            teacherDetailMap.putAll(detailMap);
        }
        for (Long userId : userList) {
            TeacherDetail teacherDetail = teacherDetailMap.get(userId);
            boolean reduce  = teacherDetail != null && grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail,"Reward",
                    "ExchangeReduction", true);
            if (!reduce) {
                continue;
            }

            if (dryRun) {
                logger.info("FixTeacherRewardIntegralJob. teacherId: {}", userId);
            }

            int num = 2000;
            String content = "补发上个月奖品中心实物扣除的园丁豆";
            IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(userId, IntegralType.其他)
                    .withIntegral(num)
                    .withComment(content)
                    .build();
            try {
                MapMessage result = userIntegralService.changeIntegral(history);
                if (!result.isSuccess()) {
                    logger.warn("FixTeacherRewardIntegralJob failed. userId:{}, Integral:{}, changeIntegralResult:{}", userId, num, result.getInfo());
                }
            } catch (Exception ex) {
                logger.error("FixTeacherRewardIntegralJob error. history {}", history, ex);
            }
        }
    }
}
