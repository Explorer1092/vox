package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.piclisten.api.CRMTextBookManagementService;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * staging运行:重新把线上数据重新生成到 staging 一份
 * 把所有教材的点读机sdkInfo 初始化。默认都是none,有 sdkBookId 的,都是外研社 sdk;
 *
 * @author jiangpeng
 * @since 2017-07-11 下午6:03
 **/
@Named
@ScheduledJobDefinition(
        jobName = "清洗点读机教材管理数据",
        jobDescription = "清洗点读机教材管理数据",
        disabled = {Mode.UNIT_TEST},
        cronExpression = "0 30 1 ? * MON",
        ENABLED = false
)
@ProgressTotalWork(100)
public class AutoTextBookManagementReloadStagingJob extends ScheduledJobWithJournalSupport {


    @ImportService(interfaceClass = CRMTextBookManagementService.class)
    private CRMTextBookManagementService crmTextBookManagementService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        if (RuntimeMode.isStaging() && SafeConverter.toBoolean(parameters.get("copyData"))) {
            String testOneId = SafeConverter.toString(parameters.get("testOneId"));
            List<TextBookManagement> textBookManagements = crmTextBookManagementService.$loadAllIgnoreEnv();
            if (StringUtils.isNotBlank(testOneId))
                textBookManagements = textBookManagements.stream().filter( t-> t.getBookId().equals(testOneId)).collect(Collectors.toList());
            List<TextBookManagement> bookManagements = textBookManagements.stream().filter(t -> !t.isStagingData()).collect(Collectors.toList());
            bookManagements.forEach(t -> {
                crmTextBookManagementService.$upsertTextBook(t);
            });
        }


        if (RuntimeMode.isStaging() && SafeConverter.toBoolean(parameters.get("deleteStaging"))){
            List<TextBookManagement> textBookManagements = crmTextBookManagementService.$loadAllIgnoreEnv();
            List<TextBookManagement> bookManagements = textBookManagements.stream().filter(t -> t.isStagingData()).collect(Collectors.toList());
            bookManagements.forEach(t -> crmTextBookManagementService.removeBookIgnoreEnv(t.getBookId()));
        }

        if (SafeConverter.toBoolean(parameters.get("initSdkInfo"))){
            crmTextBookManagementService.initSdkInfo();
        }
    }
}