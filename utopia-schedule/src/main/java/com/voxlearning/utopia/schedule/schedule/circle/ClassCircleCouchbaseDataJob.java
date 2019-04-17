package com.voxlearning.utopia.schedule.schedule.circle;


import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.entity.crm.ActivityConfig;
import com.voxlearning.utopia.schedule.cache.ScheduleCacheSystem;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.pubsub.ActivityReportProducer;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.crm.client.ActivityConfigServiceClient;

import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseKey;
import com.voxlearning.utopia.service.zone.api.entity.ClassCircleCouchBaseRecord;
import com.voxlearning.utopia.service.zone.client.ClassCircleServiceClient;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@ScheduledJobDefinition(
    jobName = "同步班级圈万圣节参加人数从couchbase到mongo",
    jobDescription = "每三分钟分钟一次",
    disabled = {Mode.DEVELOPMENT, Mode.TEST, Mode.STAGING, Mode.PRODUCTION},
    cronExpression = "0 0/1 * * * ?"
)
public class ClassCircleCouchbaseDataJob extends ScheduledJobWithJournalSupport {

  @Inject
  private ClassCircleServiceClient classCircleServiceClient;


  @Override
  protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp,
      Map <String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
    List <ClassCircleCouchBaseKey> list = classCircleServiceClient.getClazzActivityService()
        .queryCouchBaseKey();

    list.forEach(e -> {
      Long value = classCircleServiceClient.getClazzActivityService()
          .loadByKey(e.getCouchBaseKey());
      if (value == null) {
        ClassCircleCouchBaseRecord classCircleCouchBaseRecord = classCircleServiceClient
            .getClazzActivityService().queryCouchBase(e.getCouchBaseKey());
        classCircleServiceClient.getClazzActivityService()
            .setValueByKey(e.getCouchBaseKey(), classCircleCouchBaseRecord.getCouchBaseValue());
        return;
      }
      ClassCircleCouchBaseRecord classCircleCouchBaseRecord = classCircleServiceClient
          .getClazzActivityService().queryCouchBase(e.getCouchBaseKey());
      if (classCircleCouchBaseRecord == null) {
        classCircleCouchBaseRecord = new ClassCircleCouchBaseRecord();
        classCircleCouchBaseRecord.setId(e.getCouchBaseKey());
        classCircleCouchBaseRecord.setCouchBaseKey(e.getCouchBaseKey());
        classCircleCouchBaseRecord.setCouchBaseValue(value.toString());
        classCircleServiceClient.getClazzActivityService()
            .saveOrUpdateCouchBaseToMongo(classCircleCouchBaseRecord);
      } else {
        if (value > Long.valueOf(classCircleCouchBaseRecord.getCouchBaseValue())) {
          classCircleCouchBaseRecord.setCouchBaseValue(value.toString());
          classCircleServiceClient.getClazzActivityService()
              .saveOrUpdateCouchBaseToMongo(classCircleCouchBaseRecord);
        } else {
          classCircleServiceClient.getClazzActivityService()
              .setValueByKey(e.getCouchBaseKey(), classCircleCouchBaseRecord.getCouchBaseValue());
        }
      }
    });
  }

}

