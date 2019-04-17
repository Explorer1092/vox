package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.ListUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
        jobName = "自动提醒老师布置口语交际作业任务",
        jobDescription = "每天12点运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 0 12 * * ?"
)
@ProgressTotalWork(100)
public class AutoSendRemindAssignMessageJob extends ScheduledJobWithJournalSupport {
    @Inject private NewHomeworkServiceClient newHomeworkServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        List<Long> teacherIds = newHomeworkServiceClient.loadRemindAssignTeacherIds();
        if (CollectionUtils.isNotEmpty(teacherIds)) {
            List<List<Long>> teacherIdsList = ListUtils.partition(teacherIds, 1000);
            for (List<Long> teacherIdList : teacherIdsList) {
                newHomeworkServiceClient.sendRemindAssignMessage(teacherIdList);
            }
        }
    }
}
