/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

/**
 * @author Maofeng Lu
 * @since 14-10-24 下午3:36
 */
@Named
@UtopiaCacheSupport(SmartClazzQuestionReport.class)
public class SmartClazzQuestionReportDao extends StaticMongoDao<SmartClazzQuestionReport, String> {
    @Override
    protected void calculateCacheDimensions(SmartClazzQuestionReport source, Collection<String> dimensions) {
        dimensions.add(SmartClazzQuestionReport.generateCacheKeyByClazzIdSubjectQuestionId(
                source.getClazzId(), source.getSubject(), source.getQuestionId()));
    }

    @Deprecated
    public Page<SmartClazzQuestionReport> pagingFindReport(Long clazzId, Subject subject, Date start, Date end, Pageable pageable) {
        Filter filter = filterBuilder.where("clazzId").is(clazzId)
                .and("subject").is(subject)
                .and("updateAt").gte(start).lte(end);
        return __pageFind_OTF(filter.toBsonDocument(), pageable, ReadPreference.primary());
    }

    // FIXME: =================================================================
    // FIXME: 这个查询没有索引
    // FIXME: 谁是作者自己修
    // FIXME: xiaohai.zhang
    // FIXME: =================================================================
    public Page<SmartClazzQuestionReport> pagingFindReport(Long groupId, Date start, Date end, Pageable pageable) {
        Filter filter = filterBuilder.where("groupId").is(groupId)
                .and("updateAt").gte(start).lte(end);
        return __pageFind_OTF(filter.toBsonDocument(), pageable, ReadPreference.primary());
    }

    @UtopiaCacheable
    public SmartClazzQuestionReport findReportByClazzIdAndQuestionId(@UtopiaCacheKey(name = "C") Long clazzId,
                                                                     @UtopiaCacheKey(name = "S") Subject subject,
                                                                     @UtopiaCacheKey(name = "Q") String questionId) {
        Filter filter = filterBuilder.where("clazzId").is(clazzId)
                .and("subject").is(subject)
                .and("questionId").is(questionId);
        Find find = Find.find(filter).limit(1);
        return __find_OTF(find, ReadPreference.primary())
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public SmartClazzQuestionReport update(String id, SmartClazzQuestionReport smartClazzQuestionReport) {
        if (id == null || smartClazzQuestionReport == null) {
            return smartClazzQuestionReport;
        }
        SmartClazzQuestionReport oldReport = load(id);
        getCache().delete(calculateCacheDimensions(oldReport));

        __update_OTF(id, smartClazzQuestionReport);

        // update cache
        String key = SmartClazzQuestionReport.generateCacheKeyByClazzIdSubjectQuestionId(
                smartClazzQuestionReport.getClazzId(), smartClazzQuestionReport.getSubject(), smartClazzQuestionReport.getQuestionId());
        getCache().set(key, entityCacheExpirationInSeconds(), smartClazzQuestionReport);
        return smartClazzQuestionReport;
    }

    @Override
    public String insert(SmartClazzQuestionReport smartClazzQuestionReport) {
        if (smartClazzQuestionReport == null) {
            return null;
        }
        __insert_OTF(smartClazzQuestionReport);
        String id = smartClazzQuestionReport.getId();
        // add to cache
        String key = SmartClazzQuestionReport.generateCacheKeyByClazzIdSubjectQuestionId(
                smartClazzQuestionReport.getClazzId(), smartClazzQuestionReport.getSubject(), smartClazzQuestionReport.getQuestionId());
        getCache().safeAdd(key, entityCacheExpirationInSeconds(), smartClazzQuestionReport);
        return id;
    }
}
