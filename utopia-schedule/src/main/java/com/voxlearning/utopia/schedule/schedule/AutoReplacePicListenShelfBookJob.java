package com.voxlearning.utopia.schedule.schedule;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.hql.MongoCriteriaTranslator;
import com.voxlearning.alps.dao.mongo.hql.MongoUpdateTranslator;
import com.voxlearning.alps.dao.mongo.router.SyncMongoClientRouterBuilder;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author jiangpeng
 * @since 2018-03-13 下午12:17
 **/
@Named
@ScheduledJobDefinition(
        jobName = "替换点读机书架教材",
        jobDescription = "替换点读机书架教材 手动智行",
        disabled = {Mode.UNIT_TEST},
        cronExpression = "0 30 4 * * ?",
        ENABLED = false
)
public class AutoReplacePicListenShelfBookJob extends ScheduledJobWithJournalSupport {

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;


    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger, long startTimestamp, Map<String, Object> parameters, ISimpleProgressMonitor progressMonitor) throws Exception {

        //key : 被替换的 bookId   value: 新的 bookId
        Map<String, Object> map = (Map<String, Object>) parameters.get("map");
        if (MapUtils.isEmpty(map))
            return;
        if (map.size() > 1) //一次只执行一条数据
            return;
        MongoCriteriaTranslator criteriaTranslator = MongoCriteriaTranslator.INSTANCE;
        MongoUpdateTranslator updateTranslator = MongoUpdateTranslator.INSTANCE;

        MongoDatabase mongoDatabase = SyncMongoClientRouterBuilder.getInstance()
                .getSyncMongoClientRouter("mongo-jxt")
                .getDatabase("vox-jxt");
        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("piclisten_book_shelf");
        Map.Entry<String, Object> entry = map.entrySet().stream().findAny().orElse(null);
        if (entry == null)
            return;

        String oldBookId = entry.getKey();
        String targetBookId = SafeConverter.toString(entry.getValue());
        if (StringUtils.isBlank(targetBookId))
            return;
        Bson findBson = new BsonDocument()
                .append("bookId", new BsonString(oldBookId)).append("disabled", new BsonBoolean(Boolean.FALSE));
        FindIterable<Document> documents = mongoCollection.find(findBson);
        List<List<Document>> lists = splitList(documents, 100);
        int taskCount = 0;
        for (List<Document> list : lists) {
            taskCount = taskCount + list.size();
        }
        int threadCount = lists.size();
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        ISimpleProgressMonitor iSimpleProgressMonitor = progressMonitor.subTask(100, taskCount);
        for (int i = 0; i < threadCount; i++) {
            List<Document> documentList = lists.get(i);
            if (CollectionUtils.isEmpty(documentList))
                continue;
            AlpsThreadPool.getInstance().submit(() -> {
                try {
                    for (Document document : documentList) {
                        try {
                            Long parentId = document.getLong("parentId");
                            if (parentId == null || parentId == 0)
                                continue;
                            parentSelfStudyService.deleteBookFromPicListenShelf(parentId, oldBookId);
                            List<PicListenBookShelf> picListenBookShelves = parentSelfStudyService.loadParentPicListenBookShelf(parentId);
                            if (CollectionUtils.isNotEmpty(picListenBookShelves)) {
                                boolean contains = picListenBookShelves.stream().anyMatch(t -> targetBookId.equals(t.getBookId()));
                                if (!contains) {
                                    parentSelfStudyService.addBook2PicListenShelf(parentId, targetBookId);
                                }
                            } else {
                                parentSelfStudyService.addBook2PicListenShelf(parentId, targetBookId);
                            }
                        }catch (Exception e){

                        }finally {
                            iSimpleProgressMonitor.worked(1);
                        }

                    }
                } catch (Exception e) {

                } finally {
                    countDownLatch.countDown();
                }
            });
        }


    }

    public static <T> List<List<T>> splitList(Iterable<T> source, int count) {
        Validate.isTrue(count > 0);
        if (source == null) {
            return Collections.emptyList();
        }

        Map<Integer, List<T>> dest = new LinkedHashMap<>();
        int cursor = 0;
        for (T element : source) {
            if (dest.containsKey(cursor)) {
                dest.get(cursor).add(element);
            } else {
                List<T> list = new ArrayList<>();
                list.add(element);
                dest.put(cursor, list);
            }
            cursor = cursor == count - 1 ? 0 : cursor + 1;
        }
        return new ArrayList<>(dest.values());
    }
}
