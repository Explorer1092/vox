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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.question.api.entity.NewExam;
import org.bson.BsonDocument;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Named
@UtopiaCacheSupport(NewExamRegistration.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class NewExamRegistrationDao extends DynamicMongoDao<NewExamRegistration, String> {
    @Override
    protected String calculateDatabase(String template, NewExamRegistration entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, NewExamRegistration entity) {
        NewExamRegistration.ID id = entity.parseID();
        return StringUtils.formatMessage(template, id.getMonth());
    }

    @Override
    protected void calculateCacheDimensions(NewExamRegistration source, Collection<String> dimensions) {
        dimensions.add(NewExamRegistration.ck_id(source.getId()));
        dimensions.add(NewExamRegistration.ck_newExam(source.getNewExamId()));
        dimensions.add(NewExamRegistration.ck_eIdAndClazzId(source.getNewExamId(), source.getClazzId()));
    }


    /**
     * 提交成绩后处理分数、答题时长、是否完成
     *
     * @param id
     * @param score                分数
     * @param durationMilliseconds 消耗时长
     * @param finished             是否完成
     * @return boolean
     */
    public Boolean doNewExamResult(String id, Double score, Long durationMilliseconds, Boolean finished) {

        Date currentDate = new Date();
        Filter filter = filterBuilder.where("_id").is(id);
        Find find = Find.find(filter);

        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection mongoLocation = createMongoConnection(namespace);
        Update update = updateBuilder.build();
        update.set("updateAt", currentDate);
        update.set("score", score);
        update.set("durationMilliseconds", durationMilliseconds);
        if (finished) {
            update.set("finishAt", currentDate);
        }

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamRegistration modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamRegistration.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamRegistration.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamRegistration.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified != null;
    }

    /**
     * 交卷
     * @param id
     * @param clientType
     * @param clientName
     * @param durationMilliseconds
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
        if (durationMilliseconds != null) {
            update.set("durationMilliseconds", durationMilliseconds);
        }
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(false)
                .returnDocument(ReturnDocument.AFTER);

        BsonDocument document = mongoLocation.collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(find.filter(), update.toBsonDocument(), options);

        NewExamRegistration modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamRegistration.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamRegistration.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamRegistration.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified != null;
    }

    /**
     * 批改
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

        NewExamRegistration modified = transform(document);
        if (modified != null) {
            getCache().getCacheObjectModifier().modify(NewExamRegistration.ck_id(id),
                    entityCacheExpirationInSeconds(), currentValue -> modified);

            getCache().delete(NewExamRegistration.ck_newExam(modified.getNewExamId()));
            getCache().delete(NewExamRegistration.ck_eIdAndClazzId(modified.getNewExamId(), modified.getClazzId()));
        }
        return modified != null;
    }

    /**
     * 老师试卷列表
     * @param newExam
     * @param clazzId
     * return List<String>
     */
    public List<String> findByNewExamAndClazzId(NewExam newExam, Long clazzId) {

        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamRegistration.ID id = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), "0");
        MongoNamespace namespace = generateMongoNamespace(id.toString());
        String key = NewExamRegistration.ck_eIdAndClazzId(newExam.getId(), clazzId);
        List<String> list = getCache().load(key);
        if (list != null) {
            return list;
        }
        Filter filter = filterBuilder.where("newExamId").is(newExam.getId())
                .and("clazzId").is(clazzId);
        Find find = Find.find(filter);
        find.field().includes("_id");
        list = __find_OTF(find, ReadPreference.primary(), namespace).stream()
                .map(NewExamRegistration :: getId)
                .collect(Collectors.toList());
        getCache().add(key, entityCacheExpirationInSeconds(), list);
        return list;
    }

    /**
     * 根据考试查询所有的报名记录id
     * @param newExam
     * @return List<String>
     */
    public List<String> findByNewExam(NewExam newExam) {
        String month = MonthRange.newInstance(newExam.getCreatedAt().getTime()).toString();
        NewExamRegistration.ID id = new NewExamRegistration.ID(month, newExam.getSubject(), newExam.getId(), "0");
        MongoNamespace namespace = generateMongoNamespace(id.toString());
        String key = NewExamRegistration.ck_newExam(newExam.getId());
        List<String> list = getCache().load(key);
        if (list != null) {
            return list;
        }
        Filter filter = filterBuilder.where("newExamId").is(newExam.getId());
        Find find = Find.find(filter);
        find.field().includes("_id");
        list = __find_OTF(find, ReadPreference.primary(), namespace).stream()
                .map(NewExamRegistration::getId)
                .collect(Collectors.toList());
        getCache().add(key, entityCacheExpirationInSeconds(), list);
        return list;
    }
}
