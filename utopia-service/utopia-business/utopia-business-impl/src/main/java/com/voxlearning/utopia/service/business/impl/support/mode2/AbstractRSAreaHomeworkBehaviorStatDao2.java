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

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.client.FindIterable;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.random.RandomUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 这个按照指定的year和term切表
 * 主键的格式： year.term.objectId
 */
abstract public class AbstractRSAreaHomeworkBehaviorStatDao2<E extends Serializable>
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

    public List<E> findByProvinceCodes(Collection<Long> provinceCodes, Integer year, Term term) {
        if (CollectionUtils.isEmpty(provinceCodes) || year == null || term == null) {
            return Collections.emptyList();
        }
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument();
        List<BsonInt64> sList = provinceCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        filter.put("pcode", new BsonDocument("$in", new BsonArray(sList)));
        FindIterable<BsonDocument> documents = location.collection
                .withReadPreference(ReadPreference.primary())
                .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public List<E> findByCityCodes(Collection<Long> cityCodes, Integer year, Term term) {
        if (CollectionUtils.isEmpty(cityCodes) || year == null || term == null) {
            return Collections.emptyList();
        }
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument();
        List<BsonInt64> sList = cityCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        filter.put("ccode", new BsonDocument("$in", new BsonArray(sList)));
        FindIterable<BsonDocument> documents = location.collection
                .withReadPreference(ReadPreference.primary())
                .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public List<E> findByAreaCodes(Collection<String> areaCodes, Integer year, Term term) {
        if (CollectionUtils.isEmpty(areaCodes) || year == null || term == null) {
            return Collections.emptyList();
        }
        String id = year + "_" + term.getKey() + "_" + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument();
        List<BsonString> sList = areaCodes.stream().map(BsonString::new).collect(Collectors.toList());
        filter.put("acode", new BsonDocument("$in", new BsonArray(sList)));
        FindIterable<BsonDocument> documents = location.collection
                .withReadPreference(ReadPreference.primary())
                .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }
}
