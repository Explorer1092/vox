/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.user.impl.dao;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.RangeableMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;
import org.bson.BsonDocument;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2015/7/3
 */
@Named("user.CrmTeacherSummaryDao")
@CacheBean(type = CrmTeacherSummary.class)
public class CrmTeacherSummaryDao extends RangeableMongoDao<CrmTeacherSummary> {

    @Inject private ReportStatusServiceClient reportStatusServiceClient;

    @Override
    protected void calculateCacheDimensions(CrmTeacherSummary source, Collection<String> dimensions) {
        dimensions.add(CrmTeacherSummary.ck_tid(source.getTeacherId()));
        dimensions.add(CrmTeacherSummary.ck_mob(source.getSensitiveMobile()));
        dimensions.add(CrmTeacherSummary.ck_sid(source.getSchoolId()));
    }

    @Override
    protected void preprocessEntity(CrmTeacherSummary entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    @UtopiaCacheable
    public CrmTeacherSummary findByTeacherId(@UtopiaCacheKey(name = "TID") Long teacherId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return null;
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("teacherId").is(teacherId).and("disabled").is(false);
        Find find = Find.find(filter);
        CrmTeacherSummary summary = __find_OTF(find, ReadPreference.primary(), namespace).stream()
                .findFirst().orElse(null);
        if (summary != null) {
            summary.initializeIfNecessary();
        }
        return summary;
    }

    @UtopiaCacheable
    public CrmTeacherSummary findByMobile(@UtopiaCacheKey(name = "MOB") String mobile) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return null;
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("mobile").is(mobile).and("disabled").is(false);
        Find find = Find.find(filter).limit(1);
        CrmTeacherSummary summary = __find_OTF(find, ReadPreference.primary(), namespace).stream()
                .findFirst().orElse(null);
        if (summary != null) {
            summary.initializeIfNecessary();
        }
        return summary;
    }

    @CacheMethod
    public Map<Long, CrmTeacherSummary> findByTeacherIds(@CacheParameter(value = "TID", multiple = true) Collection<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptyMap();
        }
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyMap();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("teacherId").in(teacherIds).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmTeacherSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        Map<Long, CrmTeacherSummary> teacherMap = new HashMap<>();
        summaries.stream()
                .filter(t -> t != null)
                .collect(Collectors.groupingBy(CrmTeacherSummary::getTeacherId))
                .entrySet()
                .forEach(e -> teacherMap.putIfAbsent(e.getKey(), e.getValue().stream().findFirst().get()));
        return teacherMap;
    }

    @UtopiaCacheable
    public List<CrmTeacherSummary> findBySchool(@UtopiaCacheKey(name = "SID") Long schoolId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmTeacherSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        summaries.forEach(CrmTeacherSummary::initializeIfNecessary);
        return summaries;
    }

    public List<CrmTeacherSummary> findByFakeFlag(Boolean fakeTeacher,String validaionType){
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("fakeTeacher").is(fakeTeacher)
                .and("disabled").is(false)
                .and("validationType").is(validaionType);

        Find find = Find.find(filter);
        return __find_OTF(find,ReadPreference.primary(),namespace);
    }

    @UtopiaCacheable
    public Map<Long, List<CrmTeacherSummary>> findBySchools(@UtopiaCacheKey(name = "SID", multiple = true) Collection<Long> schoolIds) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyMap();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("schoolId").in(schoolIds).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmTeacherSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        summaries.forEach(CrmTeacherSummary::initializeIfNecessary);
        return summaries.stream().filter(p -> p != null).collect(Collectors.groupingBy(CrmTeacherSummary::getSchoolId));
    }

    public MapMessage updateTeacherFakeType(Long teacherId, CrmTeacherFakeValidationType type, String desc) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return MapMessage.errorMessage();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("teacherId").is(teacherId);
        Update update = updateBuilder.build();
        update.set("fakeTeacher", true);
        update.set("fakeDesc", desc);
        update.set("validationType", type.getName());

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection(namespace).collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter.toBsonDocument(), update.toBsonDocument(), options);
        CrmTeacherSummary teacherSummary = transform(document);
        if (teacherSummary == null) {
            return MapMessage.errorMessage();
        }
        if (SafeConverter.toBoolean(teacherSummary.getFakeTeacher())
                && StringUtils.equalsIgnoreCase(teacherSummary.getValidationType(), type.getName())) {
            // fix Bug #29186 判定成功之后清除这一条的缓存 By Wyc 2016-08-15
            Collection<String> cacheKeys = calculateCacheDimensions(teacherSummary);
            getCache().delete(cacheKeys);
            return MapMessage.successMessage("判定成功");
        } else {
            return MapMessage.errorMessage("判定失败");
        }
    }

    public MapMessage removeTeacherFakeType(Long teacherId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return MapMessage.errorMessage();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("teacherId").is(teacherId);
        Update update = updateBuilder.build();
        update.set("fakeTeacher", false);
        update.set("fakeDesc", null);
        update.set("validationType", null);

        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection(namespace).collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter.toBsonDocument(), update.toBsonDocument(), options);
        CrmTeacherSummary teacherSummary = transform(document);
        if (teacherSummary == null) {
            return MapMessage.errorMessage();
        }
        if (!SafeConverter.toBoolean(teacherSummary.getFakeTeacher())) {
            // 处理成功之后清除这一条的缓存 By Wyc 2016-08-15
            Collection<String> cacheKeys = calculateCacheDimensions(teacherSummary);
            getCache().delete(cacheKeys);
            return MapMessage.successMessage("处理成功");
        } else {
            return MapMessage.errorMessage("处理失败");
        }
    }

    private String getCollection() {
        ReportStatus reportStatus = reportStatusServiceClient.getReportStatusService()
                .loadReportStatus()
                .getUninterruptibly();
        if (reportStatus == null) {
            return null;
        }
        Map<String, Object> latestCollection = reportStatus.getLatestCollection();
        if (latestCollection == null) {
            return null;
        }
        Map map = (Map) latestCollection.get("vox_teacher_summary");
        if (map == null) {
            return null;
        }
        String collectionName = (String) map.get("collection_name");
        return StringUtils.isNotBlank(collectionName) ? collectionName : null;
    }

    public List<CrmTeacherSummary> findByCountyCodes( Collection<Integer> countyCodes) {
        if(CollectionUtils.isEmpty(countyCodes)){
            return Collections.emptyList();
        }
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("countyCode").in(countyCodes).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmTeacherSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        summaries.forEach(CrmTeacherSummary::initializeIfNecessary);
        return summaries;
    }
}
