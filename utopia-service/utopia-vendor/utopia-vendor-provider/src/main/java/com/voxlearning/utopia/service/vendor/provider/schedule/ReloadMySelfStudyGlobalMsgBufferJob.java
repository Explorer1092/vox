package com.voxlearning.utopia.service.vendor.provider.schedule;

import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedMySelfStudyGlobalMsgMap;
import com.voxlearning.utopia.service.vendor.impl.service.MySelfStudyGlobalMsgServiceImpl;
import com.voxlearning.utopia.service.vendor.impl.version.MySelfStudyGlobalMsgVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-06-21 下午2:26
 **/
@Named
@ScheduledJobDefinition(
        jobName = "ReloadMySelfStudyGlobalMsgBufferJob",
        cronExpression = "0 */5 * * * ?")
@ProgressTotalWork(100)
public class ReloadMySelfStudyGlobalMsgBufferJob extends ProgressedScheduleJob {

    @Inject
    private MySelfStudyGlobalMsgVersion mySelfStudyGlobalMsgVersion;

    @Inject
    private MySelfStudyGlobalMsgServiceImpl mySelfStudyGlobalMsgService;

    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) {
        long current = mySelfStudyGlobalMsgVersion.currentVersion();
        if (current != 1 && mySelfStudyGlobalMsgService.getMySelfStudyEntryLabelBuffer().getVersion() < current) {
            VersionedMySelfStudyGlobalMsgMap data = mySelfStudyGlobalMsgService.loadVersionedMySelfStudyGlobalMsgMap();
            mySelfStudyGlobalMsgService.getMySelfStudyEntryLabelBuffer().attach(data);
        }

        progressMonitor.done();
    }
}
