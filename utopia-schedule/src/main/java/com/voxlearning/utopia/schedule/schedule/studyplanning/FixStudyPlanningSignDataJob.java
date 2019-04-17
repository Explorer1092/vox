package com.voxlearning.utopia.schedule.schedule.studyplanning;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningLoader;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningConfig;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningFinishRecord;
import com.voxlearning.galaxy.service.studyplanning.cache.StudyPlanningCacheManager;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2019/01/28
 */
@Named
@ScheduledJobDefinition(
        jobName = "修复学习规划连续打卡数据",
        jobDescription = "手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 * * ?",
        ENABLED = false
)
@ProgressTotalWork(100)
public class FixStudyPlanningSignDataJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = StudyPlanningLoader.class)
    private StudyPlanningLoader studyPlanningLoader;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Long testSid = SafeConverter.toLong(parameters.get("studentId"));
        if (testSid != 0L) {
            StudyPlanningConfig config = studyPlanningLoader.loadUserStudyPlanningConfig(testSid);
            handleData(Collections.singletonList(config));
            return;
        }

        int perCount = SafeConverter.toInt(parameters.get("perCount"));
        int breakFlag = SafeConverter.toInt(parameters.get("breakFlag"));

        Pageable pageable = new PageRequest(0, perCount);
        Long studentId = 0L;
        int totalCount = 0;
        Page<StudyPlanningConfig> planningConfigPage;
        do {
            planningConfigPage  = studyPlanningLoader.loadStudyPlanningConfigForJob(pageable, studentId);
            if (planningConfigPage == null || CollectionUtils.isEmpty(planningConfigPage.getContent())) {
                break;
            }
            List<StudyPlanningConfig> configs = planningConfigPage.getContent().stream().filter(Objects::nonNull).collect(Collectors.toList());
            totalCount += configs.size();
            List<List<StudyPlanningConfig>> splitList = CollectionUtils.splitList(configs, 10);
            CountDownLatch latch = new CountDownLatch(splitList.size());
            for (List<StudyPlanningConfig> dataList : splitList) {
                AlpsThreadPool.getInstance().submit(() -> {
                    try {
                        handleData(dataList);
                    } catch (Exception e) {
                        logger.error("FixStudyPlanningSignDataJob error: ", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            studentId = configs.get(configs.size() - 1).getId();
            if (breakFlag == 1) {
                break;
            }
        } while (CollectionUtils.isNotEmpty(planningConfigPage.getContent()));
        logger.info("--------FixStudyPlanningSignDataJob data count:" + totalCount);
        progressMonitor.done();
    }

    private void handleData(List<StudyPlanningConfig> configs) {
        for (StudyPlanningConfig config : configs) {
            Long studentId = config.getId();
            List<StudyPlanningFinishRecord> finishRecords = studyPlanningLoader.loadUserFinishRecords(studentId);
            if (CollectionUtils.isNotEmpty(finishRecords)) {
                StudyPlanningFinishRecord todayRecord = finishRecords.stream().filter(e -> DayRange.current().contains(e.getFinishDate()))
                        .findFirst()
                        .orElse(null);
                Map<String, List<StudyPlanningFinishRecord>> finishMap = finishRecords.stream()
                        .collect(Collectors.groupingBy(p -> DateUtils.dateToString(p.getFinishDate(), "yyyyMMdd")));
                //累计完成天数
                long totalDays = finishMap.size();
                StudyPlanningCacheManager.INSTANCE.setFinishStudyPlanningTotalDays(studentId, totalDays);

                //连续完成天数
                long serialDays = 0;
                DayRange dayRange = todayRecord != null ? DayRange.current() : DayRange.current().previous();
                while (finishMap.keySet().contains(dayRange.toString())) {
                    serialDays++;
                    dayRange = dayRange.previous();
                }
                StudyPlanningCacheManager.INSTANCE.setFinishStudyPlanningSerialDays(studentId, serialDays);

                //如果今天有完成记录，需要把今天完成标志记录下来，防止用户完成后再加一次， 如果昨天有完成记录，上次完成时间置为昨天，用户今天完成还能增加，否则，上次完成时间置为昨天之前，用户今天完成从1开始
                if (todayRecord != null) {
                    StudyPlanningCacheManager.INSTANCE.recordTodayFinishCount(studentId);
                    StudyPlanningCacheManager.INSTANCE.recordLastFinishTime(studentId);
                } else if (serialDays != 0) {
                    StudyPlanningCacheManager.INSTANCE.recordLastFinishTimeForJob(studentId, DateUtils.addDays(new Date(), -1));
                } else {
                    StudyPlanningCacheManager.INSTANCE.recordLastFinishTimeForJob(studentId, DateUtils.addDays(new Date(), -3));
                }
            }

        }
    }
}
