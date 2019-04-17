/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newexam.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Named
@UtopiaCacheSupport(NewExamResult.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class NewExamResultDao extends DynamicMongoDao<NewExamResult, String> {
    @Override
    protected String calculateDatabase(String template, NewExamResult entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, NewExamResult entity) {

        NewExamResult.ID id = entity.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected void calculateCacheDimensions(NewExamResult source, Collection<String> dimensions) {
        dimensions.add(NewExamResult.ck_id(source.getId()));
        dimensions.add(NewExamResult.ck_newExam(source.getNewExamId()));
        dimensions.add(NewExamResult.ck_eIdAndClazzId(source.getNewExamId(), source.getClazzId()));
    }

    /**
     * 初始化结果中间表
     *
     * @param id
     * @param userId 用户ID
     * @return NewExamResult
     */
    public NewExamResult initNewExamResult(String paperId, String id, String newExamId, Subject subject, Long userId, Long clazzId, Long clazzGroupId, String clientType, String clientName) {
        Date date = new Date();
        NewExamResult newExamResult = new NewExamResult();
        newExamResult.setNewExamId(newExamId);
        newExamResult.setSubject(subject);
        newExamResult.setClazzId(clazzId);
        newExamResult.setClazzGroupId(clazzGroupId);
        newExamResult.setUserId(userId);
        newExamResult.setCreateAt(date);
        newExamResult.setClientType(clientType);
        newExamResult.setClientName(clientName);
        newExamResult.setFlightRecorderTime(date);
        newExamResult.setPaperId(paperId);
        return loadIfPresentElseInsert(id, newExamResult);
    }

    /**
     * 考试的写入操作
     *
     * @param qid       试题id
     * @param processId 考试做题明细对应的id
     * @return boolean
     * @Param score 总分
     * @Param durationMilliseconds 总时长
     * @Param finished 是否完成
     */
    public NewExamResult doNewExam(String id,
                                   String qid,
                                   String processId,
                                   Double score,
                                   Long totalDureation,
                                   Boolean finished) {
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);
        Date currentDate = new Date();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();

        update.set("answers" + "." + qid, processId);
        update.set("updateAt", currentDate);
        update.set("score", score);
        update.set("durationMilliseconds", totalDureation);
        update.set("flightRecorderTime", currentDate);
        if (finished) {
            update.set("finishAt", currentDate);
        }

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamResult modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamResult.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamResult.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamResult.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified;
    }

    /**
     * 交卷
     *
     * @param id
     * @param clientType
     * @param clientName
     * @return boolean
     */
    public Boolean submitNewExam(String id, String clientType, String clientName, Long durationMilliseconds) {

        Date currentDate = new Date();
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);

        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();
        update.set("submitAt", currentDate);
        update.set("updateAt", currentDate);
        update.set("clientType", clientType);
        update.set("clientName", clientName);
        //用于crm交卷功能
        if (durationMilliseconds != null) {
            update.set("durationMilliseconds", durationMilliseconds);
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamResult modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamResult.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamResult.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamResult.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified != null;
    }

    /**
     * 打点时间(每次进入考试需要打点，用来计算完成时长)
     *
     * @param id
     * @return boolean
     */
    public Boolean flightRecorderTime(String id) {

        Date currentDate = new Date();
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);

        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();
        update.set("flightRecorderTime", currentDate);
        update.set("updateAt", currentDate);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamResult modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamResult.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamResult.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamResult.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified != null;
    }

    /**
     * 批改
     *
     * @param id
     * @param score
     * @return boolean
     */
    public Boolean correctNewExam(String id, Double score) {

        Date currentDate = new Date();
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);

        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();
        update.set("correctScore", score);
        update.set("correctAt", currentDate);
        update.set("updateAt", currentDate);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamResult modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamResult.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamResult.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamResult.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified != null;
    }

    /**
     * 老师试卷列表
     *
     * @param newExam
     * @param clazzId return List<String>
     */
    public List<String> findByNewExamAndClazzId(NewExam newExam, Long clazzId) {

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), "0");
        MongoNamespace namespace = generateMongoNamespace(id.toString());
        String key = NewExamResult.ck_eIdAndClazzId(newExam.getId(), clazzId);
        List<String> list = getCache().load(key);
        if (CollectionUtils.isNotEmpty(list)) {
            return list;
        }
        Filter filter = filterBuilder.where("newExamId").is(newExam.getId())
                .and("clazzId").is(clazzId);
        Find find = Find.find(filter);
        find.field().includes("_id");
        list = __find_OTF(find, ReadPreference.primary(), namespace).stream()
                .map(NewExamResult::getId)
                .collect(Collectors.toList());
        getCache().add(key, entityCacheExpirationInSeconds(), list);
        return list;
    }

    /**
     * 根据考试查询到所有的resultId(job临时用到)
     */
    public List<String> findByNewExam(NewExam newExam) {
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamResult.ID id = new NewExamResult.ID(month, newExam.getSubject(), newExam.getId(), "0");
        MongoNamespace namespace = generateMongoNamespace(id.toString());
        Filter filter = filterBuilder.where("newExamId").is(newExam.getId());
        Find find = Find.find(filter);
        find.field().includes("_id");
        List<String> list = __find_OTF(find, ReadPreference.primary(), namespace).stream()
                .map(NewExamResult::getId)
                .collect(Collectors.toList());
        return list;
    }

}
