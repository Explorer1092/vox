package com.voxlearning.utopia.schedule.schedule;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.vendor.api.entity.JxtNewsBookRef;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.Document;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2017-2-7
 */
@Named
@ScheduledJobDefinition(
        jobName = "自动同步资讯和教材关联关系",
        jobDescription = "每天5:40运行一次",
        disabled = {Mode.UNIT_TEST, Mode.DEVELOPMENT, Mode.STAGING},
        cronExpression = "0 40 5 * * ?"
)
@ProgressTotalWork(100)
public class AutoSyncJxtNewsBookRefJob extends ScheduledJobWithJournalSupport {

    @Inject
    private JxtNewsServiceClient jxtNewsServiceClient;
    @Inject
    private JxtNewsLoaderClient jxtNewsLoaderClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {
        String start = SafeConverter.toString(parameters.get("start"));
        Date startTime;
        if (StringUtils.isNotBlank(start)) {
            startTime = DateUtils.stringToDate(start, DateUtils.FORMAT_SQL_DATETIME);
        } else {
            startTime = DayRange.current().getStartDate();
        }
        MongoDatabase database = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo-jxt")
                .getDatabase("vox-jxt");
        MongoCollection<Document> bookRefBigData = database.getCollection("vox_jxt_news_book_ref_big_data");
        BsonDocument filter = new BsonDocument("updateTime", new BsonDocument("$gt", new BsonDateTime(startTime.getTime())));
        long count = bookRefBigData.count(filter);
        System.out.println("AutoSyncJxtNewsBookRefJob: total count " + count);
        FindIterable<Document> documents = bookRefBigData.find(filter);
        progressMonitor.worked(10);
        ISimpleProgressMonitor monitor = progressMonitor.subTask(90, SafeConverter.toInt(count));
        long insertCount = 0;
        for (Document document : documents) {
            monitor.worked(1);
            if (document.get("_id") == null) {
                continue;
            }
            JxtNewsBookRef newsBookRef = jxtNewsLoaderClient.getJxtNewsBookRefById(SafeConverter.toString(document.get("_id")));
            if (newsBookRef == null) {
                newsBookRef = new JxtNewsBookRef();
                //已经同步过的。下面这些字段不处理了。
                newsBookRef.setId(SafeConverter.toString(document.get("_id")));
                if (document.get("bookId") == null) {
                    continue;
                }
                newsBookRef.setBookId(SafeConverter.toString(document.get("bookId")));
                if (document.get("unitId") == null) {
                    continue;
                }
                newsBookRef.setUnitId(SafeConverter.toString(document.get("unitId")));
                //英语的这个字段可能是空
                if (document.get("sectionId") != null) {
                    newsBookRef.setSectionId(SafeConverter.toString(document.get("sectionId")));
                }
                if (document.get("subjectId") == null) {
                    continue;
                }
                newsBookRef.setSubjectId(SafeConverter.toInt(document.get("subjectId")));
            }
            //任何时候同步。以大数据的newsIdList为准
            if (document.get("newsIdList") == null) {
                newsBookRef.setNewsIdList(null);
            } else {
                newsBookRef.setNewsIdList((List) (document.get("newsIdList")));
            }
            jxtNewsServiceClient.saveJxtNewsBookRef(newsBookRef);
            insertCount++;
        }
        System.out.println("AutoSyncJxtNewsBookRefJob: insert count " + insertCount);
        progressMonitor.done();
    }
}
