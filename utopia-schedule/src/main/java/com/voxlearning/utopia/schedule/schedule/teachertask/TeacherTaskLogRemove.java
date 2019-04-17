package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.concurrent.NamedDaemonThreadFactory;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhouwei on 2018/10/23
 **/
@Named
@ScheduledJobDefinition(
        jobName = "老师任务日志清理与初始化",
        jobDescription = "老师任务日志清理，负责清理半年前的数据，并且及时将老师任务转移至归档表中",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "10 10 3 ? * MON"
)
public class TeacherTaskLogRemove extends ScheduledJobWithJournalSupport {

    @Inject
    private TeacherTaskLoaderClient teacherTaskLoader;

    private Long currentTeacherId = 0L;
    private Long jobProgress = 0L;
    private Integer threadPoolSize = 10;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            threadPoolSize, threadPoolSize,
            10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(100),
            NamedDaemonThreadFactory.getInstance("TeacherTaskLogRemove-Pool"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    private synchronized List<Long> pullTaskTeacherId() {
        List<Long> taskTeacherId = teacherTaskLoader.getTaskTeacherId(currentTeacherId); // 一次拉1000

        if (CollectionUtils.isNotEmpty(taskTeacherId)) {
            currentTeacherId = taskTeacherId.get(taskTeacherId.size() - 1); // 把最大的那个设置到进度上
            printLog();
        } else {
            logger.info("老师任务日志清理与初始化任务即将结束");
        }
        return taskTeacherId;
    }

    /**
     * 3万个老师打印一下进度
     */
    private void printLog() {
        ++jobProgress;
        if (jobProgress % 30 == 0) {
            logger.info("老师任务日志清理与初始化任务进度：{} / 约6000", jobProgress);
        }
    }

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        try {
            Date now = new Date();
            Long deleteTime = now.getTime() - 180 * 24 * 60 * 60 * 1000L;//半年前的日期
            teacherTaskLoader.removeTeacherTaskLog(deleteTime);

            Runnable runnable = () -> {
                while (true) {
                    List<Long> teacherIds = pullTaskTeacherId();
                    if (CollectionUtils.isEmpty(teacherIds)) {
                        break;
                    }
                    for (Long teacherId : teacherIds) {
                        try {
                            teacherTaskLoader.getTtLoader().loadTaskProgress(teacherId);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            };

            currentTeacherId = 0L;
            jobProgress = 0L;

            for (int i = 0; i < threadPoolSize; i++) {
                executor.submit(runnable);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
