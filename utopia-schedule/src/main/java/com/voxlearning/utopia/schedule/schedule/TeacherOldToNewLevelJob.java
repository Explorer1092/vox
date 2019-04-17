package com.voxlearning.utopia.schedule.schedule;

import com.google.common.util.concurrent.RateLimiter;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.consumer.TeacherLevelServiceClient;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static com.mongodb.client.model.Filters.exists;

@Named
@ScheduledJobDefinition(
        jobName = "初始化老师新等级",
        jobDescription = "重新计算老师等级, 手动执行",
        disabled = {Mode.UNIT_TEST, Mode.STAGING, Mode.DEVELOPMENT, Mode.TEST, Mode.PRODUCTION},
        ENABLED = false,
        cronExpression = "0 0 5 * * * ? "
)
public class TeacherOldToNewLevelJob extends ScheduledJobWithJournalSupport {

    private RateLimiter rateLimiter = RateLimiter.create(100);
    private ExecutorService executor = Executors.newFixedThreadPool(8);

    @Inject private TeacherLevelServiceClient teacherLevelServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo-plat")
                .getDatabase("vox-user");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("vox_teacher_ext_attribute");
        int qps = SafeConverter.toInt(parameters.get("qps"));
        if (qps > 0) {
            rateLimiter = RateLimiter.create(qps);
        }
        // 如果是staging环境
        if (RuntimeMode.isStaging()) {
            return;
        }

        int index = 1;
        while (true) {
            // 查询没有初始化过等级的老师
            FindIterable<Document> teacherExtAttributes = mongoCollection.find(exists("initNewExpTime", false)).limit(100);
            if (teacherExtAttributes.first() == null) {
                break;
            }
            for (Document document : teacherExtAttributes) {
                rateLimiter.acquire();
                int oldLevel = SafeConverter.toInt(document.getInteger("level"));
                int exp = SafeConverter.toInt(document.getInteger("exp"));
                boolean initNewExp = document.getBoolean("initNewExp", false);
                executor.execute(() -> initNewLevel(oldLevel, exp, initNewExp, SafeConverter.toLong(document.get("_id"))));
            }
            logger.info("initLevel size : " + index++);
        }
        executor.shutdown();
    }

    private void initNewLevel(int oldLevel, int exp, boolean initNewExp, Long teacherId) {
        // 没有初始化的
        if (!initNewExp) {
            if (oldLevel >= 64) {
                exp += 1800;
            } else if (oldLevel >= 16) {
                exp += 900;
            } else if (oldLevel >= 4) {
                exp += 300;
            } else if (oldLevel >= 1) {
                exp += 1;
            }
        }
        TeacherExtAttribute.NewLevel newLevel = TeacherExtAttribute.NewLevel.getNewLevelByExp(exp);
        teacherLevelServiceClient.updateNewLevel(teacherId, newLevel.getLevel(), true);
    }
}
