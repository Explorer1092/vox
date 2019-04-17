/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.dao.jdbc.support.RowMapperWithoutResult;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.queue.UserIntegralQueueProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.config.client.GlobalTagServiceClient;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.GlobalTagName.ExpiredIntegralExcludeUsers;

/**
 * 积分过期结算任务
 *
 * @author Ruib
 * @version 0.1
 * @since 2016/3/7
 */
@Named
@ScheduledJobDefinition(
        jobName = "积分过期结算任务",
        jobDescription = "积分过期结算任务，1,4,5,6,7,10,11,12月1号运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 2 0 1 1,4,5,6,7,10,11,12 ?",
        ENABLED = true
)
@ProgressTotalWork(100)
public class AutoExpiredIntegralJob extends ScheduledJobWithJournalSupport {

    @Inject
    private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;
    @Inject
    private GlobalTagServiceClient globalTagServiceClient;
    @Inject
    private UserIntegralQueueProducer userIntegralQueueProducer;

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
        MonthRange currentMonth = MonthRange.current();

        // 黑名单
        Set<Long> blackList = globalTagServiceClient.getGlobalTagBuffer()
                .findByName(ExpiredIntegralExcludeUsers.name())
                .stream()
                .map(e -> SafeConverter.toLong(e.getTagValue()))
                .collect(Collectors.toSet());

        // 上个月的第一天，用户计算福利缓存key
        String month = DateUtils.dateToString(currentMonth.previous().getStartDate(), DateUtils.FORMAT_SQL_DATE);

        // 是否是dryRun模式
        boolean dryRun = SafeConverter.toBoolean(parameters.get("dryRun"));
        List<Long> userList = (List<Long>) parameters.get("userIds");

        if (CollectionUtils.isEmpty(userList)) {//默认执行所有的老师用户
            int tableNum = 10;//线上10张表
            if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {//测试环境2张表
                tableNum = 2;
            }
            while (tableNum > 0) {
                Set<Long> userIds = new HashSet<>();
                String tableName = "VOX_INTEGRAL_" + --tableNum;
                //查询有积分的用户
                String userSelectSQL = "SELECT ID FROM " + tableName + " WHERE USER_TYPE=1 AND USABLE_INTEGRAL > 0";
                utopiaSql.withSql(userSelectSQL).queryAll((rs, rowNum) -> userIds.add(rs.getLong("ID")));
                handleIntegral(userIds, dryRun, blackList, month);
            }
        } else {//执行输入的相关ID的老师用户
            Set<Long> userIds = userList.stream().map(SafeConverter::toLong).collect(Collectors.toSet());
            handleIntegral(userIds, dryRun, blackList, month);
        }

        progressMonitor.done();
    }

    private void handleIntegral(Set<Long> userIds, boolean dryRun, Set<Long> blackList, String month) throws InterruptedException {
        // 获取上个月获得免扣除过期园丁豆福利的老师
        Map<Long, Integer> map = asyncUserCacheServiceClient.getAsyncUserCacheService()
                .TeacherExpiredIntegralFreeCacheManager_fetchCount(userIds, month)
                .getUninterruptibly();
        int i = 1;
        for (Long userId : userIds) {
            // 黑名单中教师不处理
            if (blackList.contains(userId)) {
                continue;
            }
            if (i % 1000 == 0) {
                Thread.sleep(2000);
            }
            //是否免扣除
            boolean free = map.containsKey(userId) && map.get(userId) >= 4;
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("U", userId);
            message.put("R", dryRun);
            message.put("F",  free);
            userIntegralQueueProducer.getExpiredQueue().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(message)));
            i ++;
        }
    }
}
