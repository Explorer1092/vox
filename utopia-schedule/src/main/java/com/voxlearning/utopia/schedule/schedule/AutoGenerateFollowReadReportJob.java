package com.voxlearning.utopia.schedule.schedule;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * 取昨天和,
 *
 * @author jiangpeng
 * @since 2017-03-22 下午6:29
 **/
@Named
@ScheduledJobDefinition(
        jobName = "生成跟读报告数据",
        jobDescription = "每天凌晨运行",
        disabled = {
                Mode.UNIT_TEST,
                Mode.STAGING
        },
        cronExpression = "0 20 5 * * ?"
)
@ProgressTotalWork(100)
public class AutoGenerateFollowReadReportJob extends ScheduledJobWithJournalSupport {


    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        boolean getAll = SafeConverter.toBoolean(parameters.get("getAll"));
        Set<Long> updateStudentIds;
        if (getAll) {
            updateStudentIds = new HashSet<>();
            MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                    .getSyncMongoClientRouter("mongo-jxt")
                    .getDatabase("vox-jxt");
            Bson projection = new BsonDocument()
                    .append("studentId", new BsonBoolean(true));
            for (int i = 0; i < 100; i++) {
                MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("follow_read_sentence_result_" + i);
                FindIterable<Document> documents = mongoCollection.find().projection(projection);
                for (Document document : documents) {
                    long studentId = SafeConverter.toLong(document.get("studentId"));
                    if (studentId != 0)
                        updateStudentIds.add(studentId);
                }
            }

        } else {
            DayRange currentDay = DayRange.current();
            Set<Long> yesterdayStudentIds = parentSelfStudyService.dayFollowReadActiveStudentIdSet(currentDay.previous()); //昨天有跟读记录的学生id

            Date thirtyDayBefore = DateUtils.calculateDateDay(currentDay.getEndDate(), -30);
            Set<Long> beforeThirtyDayStudentIds = parentSelfStudyService.dayFollowReadActiveStudentIdSet(DayRange.newInstance(thirtyDayBefore.getTime()));  //30天前有跟读记录的学生id。因为报告数据30天过期,所有快过期的也要重新生成。

            updateStudentIds = union(yesterdayStudentIds, beforeThirtyDayStudentIds);
        }

        progressMonitor.worked(1);

        ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(99, updateStudentIds.size());


        List<List<Long>> sources = CollectionUtils.splitList(new ArrayList<>(updateStudentIds), 50);
        int threadCount = sources.size();
        logger.info("准备启动多线程，线程数{}", threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final List<Long> studentIdList = sources.get(i);
            AlpsThreadPool.getInstance().submit(() -> {
                for (Long studentId : studentIdList) {
                    handle(studentId, iSimpleProgressMonitor);
                }
                latch.countDown();
            });
            logger.info("线程{}/{}启动", i + 1, threadCount);
        }
        latch.await();
    }

    private void handle(Long studentId, ISimpleProgressMonitor iSimpleProgressMonitor) {
        try {
            parentSelfStudyService.updateStudentFollowSentenceResult(studentId);
        } catch (Exception e) {
            logger.error("generate follow read report data fail, studentId:" + studentId, e);
        } finally {
            iSimpleProgressMonitor.worked(1);
        }
    }


    public <T> Set<T> union(Set<T> s1, Set<T> s2) {
        Set<T> unionSet = new HashSet<T>();
        for (T s : s1) {
            unionSet.add(s);
        }
        for (T s : s2) {
            unionSet.add(s);
        }
        return unionSet;
    }
}
