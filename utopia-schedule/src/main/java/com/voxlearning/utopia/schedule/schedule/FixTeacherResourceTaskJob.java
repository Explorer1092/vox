package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceLoaderClient;
import com.voxlearning.utopia.service.business.consumer.TeachingResourceServiceClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by haitian.gan on 2017/9/7.
 */
@Named
@ScheduledJobDefinition(
        jobName = "补跑教学资源任务JOB",
        jobDescription = "补跑教学资源任务JOB",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 20 23 1 1 ?",
        ENABLED = false
)
public class FixTeacherResourceTaskJob extends ScheduledJobWithJournalSupport {

    @Inject private TeachingResourceLoaderClient teachingResourceLoaderClient;
    @Inject private TeachingResourceServiceClient teachingResourceServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Inject private RaikouSDK raikouSDK;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        Map<Long, List<TeacherResourceTask>> teacherTaskMap = teachingResourceLoaderClient.loadTasksByStatus("ONGOING")
                .stream()
                .filter(t -> SafeConverter.toString(t.getTask()).startsWith("PRO_SURVIVAL"))
                .collect(Collectors.groupingBy(t -> SafeConverter.toLong(t.getUserId())));

        AtomicInteger hasTerminalClazzNum = new AtomicInteger(0);
        teacherTaskMap.forEach((userId, tasks) -> {
            boolean hasTerminalClazz = teacherLoaderClient.loadTeacherClazzIds(userId)
                    .stream()
                    .anyMatch(cId -> {
                        Clazz clazz = raikouSDK.getClazzClient()
                                .getClazzLoaderClient()
                                .loadClazz(cId);
                        return clazz != null && clazz.isTerminalClazz();
                    });

            if (hasTerminalClazz) {
                hasTerminalClazzNum.incrementAndGet();
                teachingResourceServiceClient.supplyUserTask(userId);

                logger.info("FixTeacherResourceTaskJob:deal teacher:{}", userId);
            }
        });

        logger.info("FixTeacherResourceTaskJob: totalNum:{},hasTerminalClazzNum:{}", teacherTaskMap.size(), hasTerminalClazzNum);

    }
}
