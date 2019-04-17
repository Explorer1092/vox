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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.RangeableMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.entity.crm.CrmSchoolSummary;
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

@Named("user.CrmSchoolSummaryDao")
@CacheBean(type = CrmSchoolSummary.class)
public class CrmSchoolSummaryDao extends RangeableMongoDao<CrmSchoolSummary> {

    @Inject private ReportStatusServiceClient reportStatusServiceClient;

    @Override
    protected void calculateCacheDimensions(CrmSchoolSummary source, Collection<String> dimensions) {
        dimensions.add(CrmSchoolSummary.ck_sid(source.getSchoolId()));
    }

    @Override
    protected void preprocessEntity(CrmSchoolSummary entity) {
        super.preprocessEntity(entity);
        if (entity.getDisabled() == null) entity.setDisabled(Boolean.FALSE);
    }

    @UtopiaCacheable
    public CrmSchoolSummary findBySchoolId(@UtopiaCacheKey(name = "SID") Long schoolId) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return null;
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);

        Filter filter = filterBuilder.where("schoolId").is(schoolId).and("disabled").is(false);
        Find find = Find.find(filter).limit(1);
        return __find_OTF(find, ReadPreference.primary(), namespace).stream().findFirst().orElse(null);
    }

    @CacheMethod
    public Map<Long, CrmSchoolSummary> findBySchoolIds(@CacheParameter(value = "SID", multiple = true) Collection<Long> schoolIds) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return null;
        }

        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.where("schoolId").in(schoolIds).and("disabled").is(false);
        Find find = Find.find(filter);
        List<CrmSchoolSummary> summaries = __find_OTF(find, ReadPreference.primary(), namespace);
        Map<Long, CrmSchoolSummary> schoolMap = new HashMap<>();
        summaries.stream()
                .filter(t -> t != null)
                .collect(Collectors.groupingBy(CrmSchoolSummary::getSchoolId))
                .entrySet()
                .forEach(e -> schoolMap.putIfAbsent(e.getKey(), e.getValue().stream().findFirst().get()));
        return schoolMap;
    }

    public List<CrmSchoolSummary> findBySchoolName(String schoolName, Integer limit) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.build();
        schoolName = StringRegexUtils.escapeExprSpecialWord(schoolName);
        if (StringUtils.isNotBlank(schoolName)) {
            filter.and("schoolName").regex(".*" + schoolName + ".*", "i");
//            filter.orOperator(filterBuilder.build().and("schoolName").regex(".*" + schoolName + ".*", "i"), filterBuilder.build().and("cName").regex(".*" + schoolName + ".*", "i"));
        }
        filter.and("disabled").is(false);

        Find find = Find.find(filter);
        limit = limit <= 0 ? 40 : limit;
        return __find_OTF(find.limit(limit), ReadPreference.primary(), namespace);
    }

    public List<CrmSchoolSummary> findByCityCodesAndName(Collection<Integer> cityCodes, String schoolName) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.build();
        schoolName = StringRegexUtils.escapeExprSpecialWord(schoolName);
        if (StringUtils.isNotBlank(schoolName)) {
            filter.and("schoolName").regex(".*" + schoolName + ".*", "i");
//            filter.orOperator(filterBuilder.build().and("schoolName").regex(".*" + schoolName + ".*", "i"), filterBuilder.build().and("cName").regex(".*" + schoolName + ".*", "i"));
        }
        filter.and("cityCode").in(cityCodes);
        filter.and("disabled").is(false);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary(), namespace);
    }

    public List<CrmSchoolSummary> findByCountyCodesAndName(Collection<Integer> countyCodes, String schoolName) {
        String database = determineDatabase(null);
        String collection = getCollection();
        if (collection == null) {
            return Collections.emptyList();
        }
        MongoNamespace namespace = new MongoNamespace(database, collection);
        Filter filter = filterBuilder.build();
        schoolName = StringRegexUtils.escapeExprSpecialWord(schoolName);
        if (StringUtils.isNotBlank(schoolName)) {
            filter.and("schoolName").regex(".*" + schoolName + ".*", "i");
//            filter.orOperator(filterBuilder.build().and("schoolName").regex(".*" + schoolName + ".*", "i"), filterBuilder.build().and("cName").regex(".*" + schoolName + ".*", "i"));
        }
        filter.and("countyCode").in(countyCodes);
        filter.and("disabled").is(false);
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
        Map map = (Map) latestCollection.get("vox_school_summary");
        if (map == null) {
            return null;
        }
        String collectionName = (String) map.get("collection_name");
        return StringUtils.isNotBlank(collectionName) ? collectionName : null;
    }

}
