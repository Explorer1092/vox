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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.AbstractSweeperTask;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.schedule.support.SweeperTask;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 大扫除机器人，每天定时运行一次。
 *
 * @author Xiaohai Zhang
 * @since 2013-11-13 10:25
 */
// 0 0 2 * * ?
@Named
@ScheduledJobDefinition(
        jobName = "大扫除机器人任务",
        jobDescription = "大扫除机器人，每天4AM启动一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 4 * * ?"
)
public class AutoSweeperRobotJob extends ScheduledJobWithJournalSupport {

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        // auto scan dropins
        logger.info("大扫除机器人开始扫描dropins...");
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(SweeperTask.class));
        Set<BeanDefinition> beans = provider
                .findCandidateComponents("com.voxlearning.utopia.schedule.dropins");
        Set<String> classNames = new LinkedHashSet<>();
        logger.info("在dropins中一共发现了{}个需要执行的任务", beans.size());
        for (BeanDefinition bean : beans) {
            String className = bean.getBeanClassName();
            logger.info("任务：{}", StringUtils.substringAfterLast(className, "."));
            classNames.add(className);
        }
        logger.info("创建任务实例");
        Set<AbstractSweeperTask> tasks = new LinkedHashSet<>();
        ClassLoader classLoader = getClass().getClassLoader();
        for (String className : classNames) {
            try {
                Class<?> clz = classLoader.loadClass(className);
                SweeperTask annotation = clz.getAnnotation(SweeperTask.class);
                if (!annotation.value()) {
                    logger.info("任务{}已经被禁用", StringUtils.substringAfterLast(className, "."));
                    continue;
                }
                Object task = clz.newInstance();
                if (task instanceof AbstractSweeperTask) {
                    ((AbstractSweeperTask) task).setApplicationContext(getApplicationContext());
                    tasks.add((AbstractSweeperTask) task);
                    logger.info("任务{}创建成功", StringUtils.substringAfterLast(className, "."));
                } else {
                    logger.warn("不能识别{}", StringUtils.substringAfterLast(className, "."));
                }
            } catch (Exception ex) {
                logger.warn("创建任务{}失败", StringUtils.substringAfterLast(className, "."));
            }
        }
        if (tasks.isEmpty()) {
            logger.info("没有可以执行的任务，结束");
            return;
        }
        progressMonitor.worked(1);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(99, tasks.size());

        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("messageCommandService", messageCommandServiceClient.getMessageCommandService());

        final CountDownLatch latch = new CountDownLatch(tasks.size());
        logger.info("开始使用线程池调度执行任务");
        for (final AbstractSweeperTask task : tasks) {
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    task.execute(map);
                } catch (Exception ignored) {
                    logger.error("执行数据清除任务出错。", ignored);
                } finally {
                    latch.countDown();
                    monitor.worked(1);
                }
            });
        }
        try {
            latch.await(1, TimeUnit.DAYS);
        } catch (InterruptedException ignored) {
            logger.error(ignored.getMessage(), ignored);
        }
        logger.info("所有任务都已经启动");
        progressMonitor.done();
    }
}
