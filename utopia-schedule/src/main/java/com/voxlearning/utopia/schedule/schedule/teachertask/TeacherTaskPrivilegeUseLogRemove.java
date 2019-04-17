package com.voxlearning.utopia.schedule.schedule.teachertask;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.consumer.TeacherTaskPrivilegeServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * Created by zhouwei on 2018/10/23
 **/
@Named
@ScheduledJobDefinition(
        jobName = "老师特权使用日志清理",
        jobDescription = "老师特权使用日志清理",
        disabled = {Mode.TEST, Mode.STAGING},
        cronExpression = "10 10 4 * * ?"
)
public class TeacherTaskPrivilegeUseLogRemove extends ScheduledJobWithJournalSupport {

    @Inject
    private TeacherTaskPrivilegeServiceClient teacherTaskPrivilegeServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        try {
            Date now = new Date();
            Long deleteTime = now.getTime() - 180 * 24 * 60 * 60 * 1000L;//半年前的日期
            teacherTaskPrivilegeServiceClient.removeTeacherTaskPrivilegeUserLog(deleteTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
