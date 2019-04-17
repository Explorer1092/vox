package com.voxlearning.utopia.service.piclisten.provider.schedule;

import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.schedule.support.ProgressedScheduleJob;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.impl.service.CRMTextBookManagementServiceImpl;
import com.voxlearning.utopia.service.piclisten.impl.version.TextBookManagementVersion;
import com.voxlearning.utopia.service.vendor.api.mapper.VersionedTextBookManagementList;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Named
@ScheduledJobDefinition(
        jobName = "ReloadTextBookManagementBufferJob",
        cronExpression = "0 */5 * * * ?")
@ProgressTotalWork(100)
public class ReloadTextBookManagementBufferJob extends ProgressedScheduleJob {

    @Inject
    private CRMTextBookManagementServiceImpl crmTextBookManagementService;
    @Inject
    private TextBookManagementVersion textBookManagementVersion;
    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;


    @Override
    protected void executeJob(long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) {
        long current = textBookManagementVersion.currentVersion();
        if (current != 1 && textBookManagementLoader.getTextBookManagementBuffer().getVersion() < current) {
            VersionedTextBookManagementList data = crmTextBookManagementService.loadVersionedTextBookManagementList();
            textBookManagementLoader.getTextBookManagementBuffer().attach(data);
        }

        progressMonitor.done();
    }
}
