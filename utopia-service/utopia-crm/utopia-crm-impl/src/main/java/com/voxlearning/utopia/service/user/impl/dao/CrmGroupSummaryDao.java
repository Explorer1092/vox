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
import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.connection.IMongoConnection;
import com.voxlearning.alps.dao.mongo.dao.RangeableMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.entity.crm.CrmGroupSummary;
import com.voxlearning.utopia.entity.student.report.ReportStatus;
import com.voxlearning.utopia.service.config.client.ReportStatusServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shiwei.liao
 * @since 2015/10/9.
 */
@Named("user.CrmGroupSummaryDao")
@CacheBean(type = CrmGroupSummary.class)
public class CrmGroupSummaryDao extends RangeableMongoDao<CrmGroupSummary> {

    @Inject private ReportStatusServiceClient reportStatusServiceClient;

    @Override
    protected void calculateCacheDimensions(CrmGroupSummary source, Collection<String> dimensions) {
        dimensions.add(CrmGroupSummary.ck_tid(source.getTeacherId()));
        dimensions.add(CrmGroupSummary.ck_gid(source.getGroupId()));
        dimensions.add(CrmGroupSummary.ck_cid(source.getClazzId()));
        dimensions.add(CrmGroupSummary.ck_sid(source.getSchoolId()));
    }

    @Override
    protected void preprocessEntity(CrmGroupSummary entity) {
        super.preprocessEntity(entity);
        if (entity.getDisabled() == null) entity.setDisabled(Boolean.FALSE);
    }

    @UtopiaCacheable
    public CrmGroupSummary findByGroupId(@UtopiaCacheKey(name = "GID") Long groupId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return null;
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("groupId").is(groupId).and("disabled").is(false);
        Find find = Find.find(filter).limit(1);
        return __find_OTF(find, ReadPreference.primary(), namespace).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, CrmGroupSummary> findByGroupIds(@CacheParameter(value = "GID", multiple = true) Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }

        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyMap();
        }

        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("groupId").in(groupIds).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmGroupSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        Map<Long, CrmGroupSummary> groupMap = new HashMap<>();
        summaries.stream()
                .filter(t -> t != null)
                .collect(Collectors.groupingBy(CrmGroupSummary::getGroupId))
                .entrySet()
                .forEach(e -> groupMap.putIfAbsent(e.getKey(), e.getValue().stream().findFirst().get()));
        return groupMap;
    }

    @UtopiaCacheable
    public List<CrmGroupSummary> findByTeacherId(@UtopiaCacheKey(name = "TID") Long teacherId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("teacherId").is(teacherId).and("disabled").is(false);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary(), namespace);
    }

    @UtopiaCacheable
    public Map<Long, List<CrmGroupSummary>> findByTeacherIds(@UtopiaCacheKey(name = "TID", multiple = true) Collection<Long> teacherIds) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyMap();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("teacherId").in(teacherIds).and("disabled").is(false);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary(), namespace).stream().collect(Collectors.groupingBy(CrmGroupSummary::getTeacherId));
    }

    @UtopiaCacheable
    public List<CrmGroupSummary> findByClazzId(@UtopiaCacheKey(name = "CID") Long clazzId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("clazzId").is(clazzId).and("disabled").is(false);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary(), namespace);
    }

    @UtopiaCacheable
    public List<CrmGroupSummary> findBySchoolId(@UtopiaCacheKey(name = "SID") Long schoolId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("disabled").is(false);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary(), namespace);
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
        Map map = (Map) latestCollection.get("vox_group_summary");
        if (map == null) {
            return null;
        }
        String collectionName = (String) map.get("collection_name");
        return StringUtils.isNotBlank(collectionName) ? collectionName : null;
    }

    @UtopiaCacheable
    public Map<Long,List<CrmGroupSummary>> findByClazzIds(@UtopiaCacheKey(name = "CID",multiple = true) Collection<Long> clazzIds) {
        if (CollectionUtils.isEmpty(clazzIds)) {
            return Collections.emptyMap();
        }

        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyMap();
        }

        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("clazzId").in(clazzIds).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmGroupSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        return summaries.stream().filter(t -> t != null).collect(Collectors.groupingBy(CrmGroupSummary::getClazzId));
    }
}
