package com.voxlearning.utopia.schedule.schedule.userlevel;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.userlevel.api.UserLevelLoader;
import com.voxlearning.utopia.service.userlevel.api.UserLevelService;
import com.voxlearning.utopia.service.userlevel.api.entity.UserActivationLoginRecord;

import javax.inject.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author xinxin
 * @since 12/20/17
 */
@Named
@ScheduledJobDefinition(
        jobName = "超过7天没登录的扣活跃值",
        jobDescription = "每天凌晨2点跑job执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 2 * * ?"
)
@ProgressTotalWork(100)
public class AutoUserLevelNeverLoginOver7DaysJob extends ScheduledJobWithJournalSupport {
    @ImportService(interfaceClass = UserLevelLoader.class)
    private UserLevelLoader userLevelLoader;
    @ImportService(interfaceClass = UserLevelService.class)
    private UserLevelService userLevelService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        Integer limit = SafeConverter.toInt(parameters.get("limit"), 3000);
        Boolean dry = SafeConverter.toBoolean(parameters.get("dry"), false);
        Integer times = SafeConverter.toInt(parameters.get("times"), -1);

        LocalDateTime threshold = LocalDate.now().minusDays(7).atStartOfDay();
        Long count = userLevelLoader.getUserActivationLoginRecordCount();
        if (null == count || count <= 0) {
            return;
        }
        logger.info("NeverLoginOver7Days total count:" + count);

        long startId = 0L;
        long currentCount = 0L;
        int currentTimes = 0;
        Executor executor = Executors.newFixedThreadPool(3);
        do {
            logger.info("UserLevelLoginOver7DaysJob startId:{}", startId);

            List<UserActivationLoginRecord> records = userLevelLoader.getUserActivationLoginRecord(startId, limit);
            if (CollectionUtils.isEmpty(records)) {
                break;
            }
            //取出一下次查询的startId
            records = records.stream().sorted((r1, r2) -> r2.getId().compareTo(r1.getId())).collect(Collectors.toList());
            startId = records.get(0).getId();

            currentCount += records.size();
            ++currentTimes;
            progressMonitor.worked((int) ((currentCount / count.longValue()) * 100));
            logger.info("UserLevelLoginOver7DaysJob progress {}/{}", currentCount, count);

            List<UserActivationLoginRecord> finalRecords = records;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (UserActivationLoginRecord record : finalRecords) {
                        if (dry) {
                            continue;
                        }

                        if (record.getLastLogin().toInstant().isAfter(threshold.atZone(ZoneId.systemDefault()).toInstant())) {
                            continue;
                        }

                        if (record.getUserType() == UserType.STUDENT.getType()) {
                            userLevelService.studentNotLoginOver7Days(record.getId());
                        } else if (record.getUserType() == UserType.PARENT.getType()) {
                            userLevelService.parentNotLoginOver7Days(record.getId());
                        }
                    }
                }
            });

            if (times > 0 && currentTimes >= times) {
                break;
            }
        } while (true);
    }
}
