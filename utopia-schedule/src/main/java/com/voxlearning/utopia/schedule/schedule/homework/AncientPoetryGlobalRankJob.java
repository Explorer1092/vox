package com.voxlearning.utopia.schedule.schedule.homework;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.AncientPoetryServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "古诗活动排行数据统计",
        jobDescription = "古诗活动排行榜，每天早上七点执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = " 0 0 7 * * ?"
)
@ProgressTotalWork(100)
public class AncientPoetryGlobalRankJob extends ScheduledJobWithJournalSupport {
    @Inject private AncientPoetryLoaderClient ancientPoetryLoaderClient;
    @Inject private AncientPoetryServiceClient ancientPoetryServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Long> allSchoolIds = ancientPoetryLoaderClient.loadAllJoinedStudentsSchoolIds();
        List<Integer> allRegionIds = ancientPoetryLoaderClient.loadAllJoinedStudentsRegionIds();
        if (CollectionUtils.isNotEmpty(allSchoolIds)) {
            for (Long schoolId : allSchoolIds) {
                for (int clazzLevel = 1; clazzLevel <= 6; clazzLevel++) {
                    ancientPoetryServiceClient.generateGlobalRankBySchoolIdAndClazzLevel(schoolId, clazzLevel);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(allRegionIds)) {
            for (Integer regionId : allRegionIds) {
                for (int clazzLevel = 1; clazzLevel <= 6; clazzLevel++) {
                    ancientPoetryServiceClient.generateGlobalRankByRegionIdAndClazzLevel(regionId, clazzLevel);
                }
            }
        }
    }
}
