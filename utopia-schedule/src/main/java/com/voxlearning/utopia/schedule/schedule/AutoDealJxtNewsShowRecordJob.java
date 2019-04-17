package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsParentShowRecord;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2016-9-20
 */
@Named
@ScheduledJobDefinition(
        jobName = "定期删除曝光记录中6天以前的记录",
        jobDescription = "每天5点40运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 40 5 * * ? "
)
public class AutoDealJxtNewsShowRecordJob extends ScheduledJobWithJournalSupport {
    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject
    private JxtNewsServiceClient jxtNewsServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        jobJournalLogger.log("清理资讯曝光记录任务开始");
        Pageable pageable = new PageRequest(0, 1000);
        Page<JxtNewsParentShowRecord> recordWithPage = jxtNewsLoaderClient.getShowRecordWithPage(pageable);
        int totalPages = recordWithPage.getTotalPages();
        progressMonitor.begin(totalPages);
        if (recordWithPage.getTotalElements() == 0) {
            jobJournalLogger.log("没有需要处理的用户");
            progressMonitor.done();
            return;
        }
        //开始处理
        removeShowRecord(recordWithPage, progressMonitor);
        while (recordWithPage.hasNext()) {
            pageable = recordWithPage.nextPageable();
            recordWithPage = jxtNewsLoaderClient.getShowRecordWithPage(pageable);
            removeShowRecord(recordWithPage, progressMonitor);
        }
        progressMonitor.done();
    }

    private void removeShowRecord(Page<JxtNewsParentShowRecord> recordWithPage,  ISimpleProgressMonitor progressMonitor) {
        if (recordWithPage == null || !recordWithPage.hasContent()) {
            return;
        }
        List<JxtNewsParentShowRecord> parentShowRecords = recordWithPage.getContent();
        if (CollectionUtils.isEmpty(parentShowRecords)) {
            return;
        }
        for (JxtNewsParentShowRecord record : parentShowRecords) {
            if (record == null || CollectionUtils.isEmpty(record.getShowRecordList())) {
                continue;
            }
            Set<String> needRemoveNewsIds = record.getShowRecordList()
                    .stream()
                    .filter(p -> p.getExpireTime() == null || p.getExpireTime().before(new Date()))
                    .map(JxtNewsParentShowRecord.ShowRecord::getNewsId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(needRemoveNewsIds)) {
                continue;
            }
            jxtNewsServiceClient.removeShowRecord(record.getId(), needRemoveNewsIds);
        }
        progressMonitor.worked(1);
    }
}
