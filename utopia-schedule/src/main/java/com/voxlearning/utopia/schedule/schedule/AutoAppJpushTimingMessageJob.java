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
import com.voxlearning.alps.core.concurrent.ThreadUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.push.api.entity.AppJpushTimingMessage;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 29/7/2016
 * 发送jpush定时消息
 */
@Named
@ScheduledJobDefinition(
        jobName = "发送Jpush定时消息",
        jobDescription = "发送Jpush定时消息,5分钟运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 */5 * * * ? "
)
@ProgressTotalWork(100)
public class AutoAppJpushTimingMessageJob extends ScheduledJobWithJournalSupport {

    private static final Integer SEND_TIME_DISCRETE_BASED_SECONDS = 5 * 60;

    @Inject
    private AppMessageServiceClient appMessageServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        Long sendTimeEpochSecond = getSendTime();
        int pageSize = 50000;
        int pageNum = 1;
        Page<AppJpushTimingMessage> messagePage;
        do {
            Pageable pageable = PageableUtils.startFromOne(pageNum++, pageSize);
            messagePage = appMessageServiceClient.getTimingMessage(sendTimeEpochSecond, pageable);
            if (null == messagePage || messagePage.getTotalElements() == 0) break;
            List<AppJpushTimingMessage> messages = messagePage.getContent();

            int pageCount = messagePage.getTotalPages();
            int pageStepSeconds = (SEND_TIME_DISCRETE_BASED_SECONDS - pageCount * 2) / pageCount;   //每一页可用的发送时长
            if (pageStepSeconds > 2) {
                send(messages, pageStepSeconds);
            } else {
                send(messages, 0);
            }

        } while (!messagePage.isLast());

        progressMonitor.done();
    }

    private void send(List<AppJpushTimingMessage> messages, int pageStepSeconds) {
        int segmentSize = 100;
        int segmentCount = messages.size() / segmentSize;
        if (messages.size() % segmentSize > 0) segmentCount++;
        int segmentStepMills = (pageStepSeconds * 1000) / segmentCount; //每次dubbo调用的间隔时间

        long timestamp = System.currentTimeMillis();
        List<AppJpushTimingMessage> msgs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            msgs.add(messages.get(i));
            if (msgs.size() >= segmentSize || i == messages.size() - 1) {

                appMessageServiceClient.sendAppJpushTimingMessage(msgs);
                msgs.clear();

                long spendMills = System.currentTimeMillis() - timestamp;
                if (spendMills < segmentStepMills) {
                    ThreadUtils.sleepCurrentThread(segmentStepMills - spendMills);
                }
                timestamp = System.currentTimeMillis();
            }
        }
    }

    private Long getSendTime() {
        Long originepochSecond = Instant.now().toEpochMilli() / 1000;

        if (originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS == 0) {
            return originepochSecond;
        }

        return originepochSecond - originepochSecond % SEND_TIME_DISCRETE_BASED_SECONDS;
    }
}
