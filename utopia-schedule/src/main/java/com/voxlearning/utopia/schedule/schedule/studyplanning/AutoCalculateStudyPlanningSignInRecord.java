package com.voxlearning.utopia.schedule.schedule.studyplanning;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.galaxy.service.studyplanning.api.StudyPlanningLoader;
import com.voxlearning.galaxy.service.studyplanning.api.entity.StudyPlanningFinishRecord;
import com.voxlearning.galaxy.service.studyplanning.api.entity.UserDailyStudyPlanningConfig;
import com.voxlearning.galaxy.service.studyplanning.cache.StudyPlanningCacheManager;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author malong
 * @since 2018/12/05
 */
@Named
@ScheduledJobDefinition(
        jobName = "计算学习规划连续打卡天数和累计打卡天数",
        jobDescription = "每天03：00运行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 3 * * ?"
)
@ProgressTotalWork(100)
public class AutoCalculateStudyPlanningSignInRecord extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = StudyPlanningLoader.class)
    private StudyPlanningLoader studyPlanningLoader;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Long studentId = SafeConverter.toLong(parameters.get("sid"));
        String dateStr = SafeConverter.toString(parameters.get("date"));
        if (StringUtils.isNotBlank(dateStr)) {
            Date testDate = DateUtils.stringToDate(SafeConverter.toString(parameters.get("date")),"yyyy-MM-dd");
            String dailyId = UserDailyStudyPlanningConfig.generateId(studentId, testDate);
            UserDailyStudyPlanningConfig dailyConfig = studyPlanningLoader.loadDailyConfig(dailyId);
            handleSignInRecord(Collections.singletonList(dailyConfig), DayRange.newInstance(testDate.getTime()));
            return;
        }

        Date date = DayRange.current().previous().getStartDate();
        List<UserDailyStudyPlanningConfig> dailyConfigs;
        do {
            dailyConfigs = studyPlanningLoader.loadUserDailyConfigs(date, 1000);
            if (CollectionUtils.isEmpty(dailyConfigs)) {
                break;
            }
            handleSignInRecord(dailyConfigs, DayRange.current().previous());
            UserDailyStudyPlanningConfig dailyConfig = dailyConfigs.get(dailyConfigs.size() - 1);
            date = dailyConfig.getCreateTime();
        } while (CollectionUtils.isNotEmpty(dailyConfigs));
        progressMonitor.done();

    }

    private void handleSignInRecord(List<UserDailyStudyPlanningConfig> dailyConfigs, DayRange yesterdayRange) {
        for (UserDailyStudyPlanningConfig dailyConfig : dailyConfigs) {
            if (MapUtils.isNotEmpty(dailyConfig.getItemIdMap())) {
                Long studentId = SafeConverter.toLong(dailyConfig.getId().split("-")[0]);
                Set<String> itemIds = new HashSet<>(dailyConfig.getItemIdMap().keySet());
                List<StudyPlanningFinishRecord> finishRecords = studyPlanningLoader.loadMonthFinishRecords(studentId)
                        .stream()
                        .filter(finishRecord -> yesterdayRange.contains(finishRecord.getFinishDate()))
                        .filter(finishRecord -> itemIds.contains(finishRecord.getItemId()))
                        .collect(Collectors.toList());
                if (finishRecords.size() == itemIds.size()) {
                    //累计打卡天数加1
                    StudyPlanningCacheManager.INSTANCE.incrSignInTotalDays(studentId);
                    //上一次打卡时间没有或者是前天，连续打卡天加1，并且更新上次打卡时间
                    //否则删除连续打卡天数的记录,从1开始
                    Date lastSignInDate = StudyPlanningCacheManager.INSTANCE.getSignInTime(studentId);
                    if (lastSignInDate == null || yesterdayRange.previous().contains(lastSignInDate)) {
                        StudyPlanningCacheManager.INSTANCE.incrSignInSerialDays(studentId);
                    } else {
                        StudyPlanningCacheManager.INSTANCE.resetSignInSerialDays(studentId);
                        StudyPlanningCacheManager.INSTANCE.incrSignInSerialDays(studentId);
                    }
                    StudyPlanningCacheManager.INSTANCE.recordSignInTime(studentId, yesterdayRange.getStartDate());
                }
            }
        }
    }
}
