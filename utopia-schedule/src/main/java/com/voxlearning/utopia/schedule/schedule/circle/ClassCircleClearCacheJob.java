package com.voxlearning.utopia.schedule.schedule.circle;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.zone.client.ClassCircleServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@ScheduledJobDefinition(
    jobName = "班级圈每周一清除每日一句缓存",
    jobDescription = "每周一0点删除执行",
    disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
    cronExpression = "0 0 0 ? * MON"
)
public class ClassCircleClearCacheJob extends ScheduledJobWithJournalSupport {

  @Inject
  private ClassCircleServiceClient classCircleServiceClient;


  @Override
  protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp,
      Map <String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
    for (int i = 0; i < 10; i++) {
      classCircleServiceClient.
          getClazzActivityService().
          deleteCache("com.voxlearning.utopia.service.zone.api.entity.WeekDailySentence:20181024:WeekDailySentence");
    }

  }

}

