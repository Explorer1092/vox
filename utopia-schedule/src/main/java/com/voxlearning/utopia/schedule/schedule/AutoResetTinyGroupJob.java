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
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.consumer.TinyGroupServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author RuiBao
 * @since 11/11/2015
 */
@Named
@ScheduledJobDefinition(
        jobName = "小组长轮组任务",
        jobDescription = "每周一运行一次，打散学生创建的小组",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 15 3 ? * MON")
@ProgressTotalWork(100)
public class AutoResetTinyGroupJob extends ScheduledJobWithJournalSupport {

    @Inject private TinyGroupServiceClient tinyGroupServiceClient;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

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
        Date threshold = WeekRange.current().previous().getStartDate();

        // 获取所有学生创建的小组id
        String sql = "SELECT ID FROM VOX_TINY_GROUP WHERE CREATE_DATETIME>? AND CREATOR_USER_TYPE=3 AND DISABLED=0";
        List<Long> tinyGroupIds = utopiaSql.withSql(sql).useParamsArgs(threshold).queryColumnValues(Long.class);
        if (CollectionUtils.isEmpty(tinyGroupIds)) return;
        logger.info("TOTAL {} TINY GROUP FOUND", tinyGroupIds.size());

        // 分片执行
        List<List<Long>> sources = CollectionUtils.splitList(tinyGroupIds, 5);
        int threadCount = sources.size();
        logger.info("SPLIT SOURCE DATA INTO {} THREADS", threadCount);

        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (List<Long> source : sources) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    handleTinyGroup(source);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(2, TimeUnit.HOURS);
        } catch (InterruptedException ignored) {
            logger.warn(ignored.getMessage(), ignored);
        }
        progressMonitor.done();
    }

    private void handleTinyGroup(List<Long> tinyGroupIds) {
        long threadId = Thread.currentThread().getId();
        logger.info("[THREAD {}] TOTAL TINY GROUP COUNT {} ...", threadId, tinyGroupIds.size());
        int j = 1;
        for (Long tinyGroupId : tinyGroupIds) {
            tinyGroupServiceClient.deleteTinyGroupForAutoResetTinyGroupJob(tinyGroupId);
            j++;
            if (RuntimeMode.ge(Mode.STAGING)) {
                if (j % 1000 == 0) {
                    logger.info("[THREAD {}] {} LEFT ...", threadId, (tinyGroupIds.size() - j));
                }
            } else {
                if (j % 10 == 0) {
                    logger.info("[THREAD {}] {} LEFT ...", threadId, (tinyGroupIds.size() - j));
                }
            }
        }
    }
}
