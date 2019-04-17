package com.voxlearning.utopia.schedule.schedule.chips;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExt;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author xuan.zhu
 * @date 2018/9/17 12:08
 * 薯条英语用户额外信息同步任务
 */
@Named
@ScheduledJobDefinition(
        jobName = "薯条英语用户额外信息数据同步任务",
        jobDescription = "任务同步手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 16 * * ?",
        ENABLED = false

)
public class ChipsEnglishUserExtSplitSyncJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        // 直接同步
        chipsEnglishUserLoader.transferUserExtToUserExtSplit();
    }
}
