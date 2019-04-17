package com.voxlearning.utopia.schedule.schedule.vendor;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.etl.VendorUserMappingProcess;

import javax.inject.Named;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "手动执行Vendor用户数据映射",
        jobDescription = "手动执行",
        ENABLED = false,
        cronExpression = "0 0 0 0 0 ? * "
)
@ProgressTotalWork(100)
public class ManualVendorUserMappingJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = VendorUserMappingProcess.class)
    VendorUserMappingProcess vendorUserMappingProcess;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {

        String cmd = SafeConverter.toString(parameters.get("cmd"));
        if(StringUtils.isNotBlank(cmd)){
            vendorUserMappingProcess.exec(cmd);
        }else{
            jobJournalLogger.log("cmd is null");
            logger.info("cmd is null");
        }

    }
}
