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
import com.mongodb.client.MongoCollection;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.dao.support.MongoConnection;
import com.voxlearning.alps.random.RandomUtils;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonInt64;

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
abstract public class AbstractRSSchoolStatDao2<E extends Serializable>
        extends DynamicMongoDao<E, String> {

    @Override
    protected void calculateCacheDimensions(E source, Collection<String> dimensions) {
    }

    @Override
    protected Class<String> doDeclareIdClass() {
        return String.class;
    }

    @Override
    protected void preprocessEntity(E entity) {
        String id = getEntityId(entity);
        String[] segments = StringUtils.split(id, ".");
        if (segments.length != 3) {
            throw new IllegalArgumentException("Invalid id '" + id + "'");
        }
    }

    @Override
    protected String calculateDatabase(String template, E entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, E entity) {
        String id = getEntityId(entity);
        String[] segments = StringUtils.split(id, ".");
        if (segments.length != 3) {
            throw new IllegalArgumentException("Invalid id '" + id + "'");
        }
        String name = segments[0] + "_" + segments[1];
        return StringUtils.formatMessage(template, name);
    }

    private MongoCollection<BsonDocument> collection(Integer year, Term term) {
        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);
        return location.collection;
    }

    public List<E> findByAreaCode(Long areaCode, Integer year, Term term) {
        if (areaCode == null || year == null || term == null) {
            return Collections.emptyList();
        }
        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument("acode", new BsonInt64(areaCode));
        FindIterable<BsonDocument> documents = location.collection
                .withReadPreference(ReadPreference.primary())
                .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public List<E> findByAreaCodes(Collection<Long> areaCodes, Integer year, Term term) {
        areaCodes = CollectionUtils.toLinkedHashSet(areaCodes);
        if (areaCodes.isEmpty() || year == null || term == null) {
            return Collections.emptyList();
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        List<BsonInt64> ccodes = areaCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("acode", new BsonDocument("$in", new BsonArray(ccodes)));
        FindIterable<BsonDocument> documents = location.collection
                .withReadPreference(ReadPreference.primary())
                .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public long countByCityCode(Long cityCide, Integer year, Term term) {
        if (cityCide == null) {
            return 0;
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument("ccode", new BsonInt64(cityCide));
        return location.collection.withReadPreference(ReadPreference.primary())
                .count(filter);
    }

    public long countByCityCodes(Collection<Long> cityCodes, Integer year, Term term) {
        cityCodes = CollectionUtils.toLinkedHashSet(cityCodes);
        if (cityCodes.isEmpty()) {
            return 0;
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        List<BsonInt64> ccodes = cityCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("ccode", new BsonDocument("$in", new BsonArray(ccodes)));
        return location.collection.withReadPreference(ReadPreference.primary()).count(filter);
    }

    public long countByCityCodesAndIsValid(Collection<Long> cityCodes, Boolean isValid, Integer year, Term term) {
        cityCodes = CollectionUtils.toLinkedHashSet(cityCodes);
        if (cityCodes.isEmpty()) {
            return 0;
        }
        if (isValid == null) {
            isValid = true;
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        List<BsonInt64> ccodes = cityCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("ccode", new BsonDocument("$in", new BsonArray(ccodes)))
                .append("isValid", new BsonBoolean(isValid));
        return location.collection.withReadPreference(ReadPreference.primary()).count(filter);
    }

    public long countByAreaCodeAndIsValid(Long areaCode, Boolean isValid, Integer year, Term term) {
        if (areaCode == null) {
            return 0;
        }
        if (isValid == null) {
            isValid = true;
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        BsonDocument filter = new BsonDocument()
                .append("acode", new BsonInt64(areaCode))
                .append("isValid", new BsonBoolean(isValid));
        return location.collection.withReadPreference(ReadPreference.primary())
                .count(filter);
    }

    public long countByAreaCodesAndIsValid(Collection<Long> areaCodes, Boolean isValid, Integer year, Term term) {
        areaCodes = CollectionUtils.toLinkedHashSet(areaCodes);
        if (areaCodes.isEmpty()) {
            return 0;
        }
        if (isValid == null) {
            isValid = true;
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        List<BsonInt64> ccodes = areaCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("acode", new BsonDocument("$in", new BsonArray(ccodes)))
                .append("isValid", new BsonBoolean(isValid));
        return location.collection.withReadPreference(ReadPreference.primary()).count(filter);
    }

    public long countByAreaCodes(Collection<Long> areaCodes, Integer year, Term term) {
        areaCodes = CollectionUtils.toLinkedHashSet(areaCodes);
        if (areaCodes.isEmpty()) {
            return 0;
        }

        String id = year + "." + term.getKey() + "." + RandomUtils.nextObjectId();
        MongoNamespace namespace = generateMongoNamespace(id);
        MongoConnection location = createMongoConnection(namespace);

        List<BsonInt64> ccodes = areaCodes.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("acode", new BsonDocument("$in", new BsonArray(ccodes)));
        return location.collection.withReadPreference(ReadPreference.primary()).count(filter);
    }

    // ========================================================================
    // schoolId
    // ========================================================================

    public List<E> findBySchoolIds(Collection<Long> schoolIds, Integer year, Term term) {
        schoolIds = CollectionUtils.toLinkedHashSet(schoolIds);
        if (schoolIds.isEmpty() || year == null || term == null) {
            return Collections.emptyList();
        }
        List<BsonInt64> ccodes = schoolIds.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("schoolId", new BsonDocument("$in", new BsonArray(ccodes)));
        FindIterable<BsonDocument> documents = collection(year, term)
                .withReadPreference(ReadPreference.primary())
                .find(filter);
        List<E> list = new ArrayList<>();
        for (BsonDocument document : documents) {
            CollectionUtils.addNonNullElement(list, transform(document));
        }
        return list;
    }

    public long countBySchoolIds(Collection<Long> schoolIds, Integer year, Term term) {
        schoolIds = CollectionUtils.toLinkedHashSet(schoolIds);
        if (schoolIds.isEmpty()) {
            return 0;
        }
        List<BsonInt64> ccodes = schoolIds.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("schoolId", new BsonDocument("$in", new BsonArray(ccodes)));
        return collection(year, term).withReadPreference(ReadPreference.primary()).count(filter);
    }

    public long countBySchoolIdsAndIsValid(Collection<Long> schoolIds, Boolean isValid, Integer year, Term term) {
        schoolIds = CollectionUtils.toLinkedHashSet(schoolIds);
        if (schoolIds.isEmpty()) {
            return 0;
        }
        if (isValid == null) {
            isValid = true;
        }
        List<BsonInt64> ccodes = schoolIds.stream().map(BsonInt64::new).collect(Collectors.toList());
        BsonDocument filter = new BsonDocument()
                .append("schoolId", new BsonDocument("$in", new BsonArray(ccodes)))
                .append("isValid", new BsonBoolean(isValid));
        return collection(year, term).withReadPreference(ReadPreference.primary()).count(filter);
    }
}
