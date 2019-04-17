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

package com.voxlearning.utopia.service.business.impl.support.mode2;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Block;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.random.RandomUtils;
import org.bson.*;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 这个按照指定的year和term切表
 * 主键的格式： year.term.objectId
 */
abstract public class AbstractRSSchoolHomeworkBehaviorStatDao2<E extends Serializable>
        extends DynamicMongoDao<E, String> {

    @Override
    protected Class<String> doDeclareIdClass() {
        return String.class;
    }

    @Override
    protected String calculateDatabase(String template, E entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, E entity) {
        String id = getEntityId(entity);
        String[] segments = StringUtils.split(id, "_");
        if (segments.length != 3) {
            throw new IllegalArgumentException("Invalid id '" + id + "'");
        }
        String name = segments[0] + "_" + segments[1];
        return StringUtils.formatMessage(template, name);
    }

    @Override
    protected void calculateCacheDimensions(E source, Collection<String> dimensions) {

    }

    public void clearData(Integer year, Term term) {
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        location.collection.deleteMany(new BsonDocument());
    }

    public List<E> findBySchoolIds_withoutIdSet(Collection<String> schoolIds, Integer year, Term term) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument();
        List<BsonString> sList = schoolIds.stream().map(BsonString::new).collect(Collectors.toList());
        filter.put("schoolId", new BsonDocument("$in", new BsonArray(sList)));
        FindIterable<BsonDocument> documents =
                location.collection
                        .withReadPreference(ReadPreference.primary())
                        .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public List<E> findSchoolDataByCityCodes_withoutIdSet(Collection<Long> cityCodes, Integer year, Term term) {
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument();
        List<BsonInt64> sList = cityCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        filter.put("ccode", new BsonDocument("$in", new BsonArray(sList)));
        filter.put("schoolId", new BsonDocument("$ne", new BsonInt32(-1)));
        FindIterable<BsonDocument> documents =
                location.collection
                        .withReadPreference(ReadPreference.primary())
                        .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public List<E> findSchoolDataByAreaCodes_withoutIdSet(Collection<Long> areaCodes, Integer year, Term term) {
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument();
        List<BsonInt64> sList = areaCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        filter.put("acode", new BsonDocument("$in", new BsonArray(sList)));
        filter.put("schoolId", new BsonDocument("$ne", new BsonInt32(-1)));
        FindIterable<BsonDocument> documents =
                location.collection
                        .withReadPreference(ReadPreference.primary())
                        .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public void updateStudentAndTeacherDataById(String schollId, Collection<Long> studentIds, Collection<Long> teacherIds, Long studentTimes, Long teacherTimes,
                                                Integer year, Term term) {
        String id = year + "_" + term.getKey() + "_" + schollId;
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

//        Update update = updateBuilder.build();
//        update.addToSet("stuIds", BasicDBObjectBuilder.start("$each", studentIds).get());
//        update.addToSet("teacherIds", BasicDBObjectBuilder.start("$each", teacherIds).get());
//        update(id, update);
//
//        location.collection.aggregate(Arrays.asList(
//                new Document("$match", new Document("_id", id))
//                , new Document("$project",
//                        new Document("stuNum", new Document("$size", "$stuIds"))
//                                .append("teacherNum", new Document("$size", "$teacherIds"))
//                                .append("schoolId", 1)
//                                .append("schoolName", 1)
//                                .append("acode", 1)
//                                .append("areaName", 1)
//                                .append("ccode", 1)
//                                .append("cityName", 1)
//                                .append("pcode", 1)
//                                .append("provinceName", 1)
//                                .append("stuIds", 1)
//                                .append("teacherIds", 1)
//                                .append("stuTimes", new Document("$add", Arrays.asList("$stuTimes", studentTimes)))
//                                .append("teacherTimes", new Document("$add", Arrays.asList("$teacherTimes", teacherTimes)))
//                                .append("createAt", 1)
//                                .append("updateAt", 1)
//                                .append("_id", 1)
//                )
//                , new Document("$out", namespace.getCollectionName())
//        )).batchSize(1).first();

        // $out Replace Existing Collection, but not replace exising document in the existing collection, so not suitable for this case.
        // Ref: http://docs.mongodb.org/manual/reference/operator/aggregation/out/

        Update update = updateBuilder.build();
        if (!CollectionUtils.isEmpty(studentIds))
            update.addToSet("stuIds", BasicDBObjectBuilder.start("$each", studentIds).get());
        if (!CollectionUtils.isEmpty(teacherIds))
            update.addToSet("teacherIds", BasicDBObjectBuilder.start("$each", teacherIds).get());
        if (studentTimes != null)
            update.inc("stuTimes", studentTimes);
        if (teacherTimes != null)
            update.inc("teacherTimes", teacherTimes);
        if (update.hasContent())
            update(id, update);

        AggregateIterable<BsonDocument> iterable = location.collection.aggregate(Arrays.asList(
                new Document("$match", new Document("_id", id))
                , new Document("$project",
                        new Document("stuNum", new Document("$size", "$stuIds"))
                                .append("teacherNum", new Document("$size", "$teacherIds"))
                )
        ));
        iterable.forEach(new Block<BsonDocument>() {
            @Override
            public void apply(BsonDocument bsonDocument) {
                Update update2 = updateBuilder.build();
                update2.set("stuNum", bsonDocument.get("stuNum"));
                update2.set("teacherNum", bsonDocument.get("teacherNum"));
                update(id, update2);
            }
        });
    }
}
