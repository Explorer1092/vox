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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.utopia.service.business.api.entity.RSOralPaperAnalysisReport;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author changyuan.liu
 * @since 2015/5/19
 */
@Named
public class RSOralPaperAnalysisReportDao extends StaticMongoDao<RSOralPaperAnalysisReport, String> {
    @Override
    protected void calculateCacheDimensions(RSOralPaperAnalysisReport source, Collection<String> collection) {
    }

    /**
     * 根据push id返回口语试卷分析结果
     *
     * @param pushId
     * @return
     * @author changyuan.liu
     */
    public List<RSOralPaperAnalysisReport> findByPushId(Long pushId) {
        return findByPushId(pushId, null, null);
    }

    /**
     * 根据push id返回口语试卷分析结果
     *
     * @param pushId
     * @param includeFields 结果需要包含的field
     * @param excludeFields 结果不需要包含的field
     * @return
     * @author changyuan.liu
     */
    public List<RSOralPaperAnalysisReport> findByPushId(Long pushId, Collection<String> includeFields, Collection<String> excludeFields) {
        if (pushId == null) {
            return Collections.emptyList();
        }
        Find find = Find.find(filterBuilder.where("pid").is(pushId));
        if (CollectionUtils.isNotEmpty(includeFields)) {
            includeFields.forEach(f -> find.field().includes(f));
        }
        if (CollectionUtils.isNotEmpty(excludeFields)) {
            excludeFields.forEach(f -> find.field().excludes(f));
        }
        return __find_OTF(find, ReadPreference.primary());
    }

    /**
     * 根据push id集合返回口语试卷分析结果
     *
     * @param pushIds
     * @return
     * @author changyuan.liu
     */
    public List<RSOralPaperAnalysisReport> findByPushIds(Collection<Long> pushIds) {
        if (CollectionUtils.isEmpty(pushIds)) {
            return Collections.emptyList();
        }
        Find find = Find.find(filterBuilder.where("pid").in(pushIds));
        return __find_OTF(find, ReadPreference.primary());
    }

    /**
     * upsert one report
     *
     * @param report
     * @author changyuan.liu
     */
    public void upsert(RSOralPaperAnalysisReport report) {
        if (report.getId() == null) {
            insert(report);
        } else {
            update(report.getId(), report);
        }
    }

    public RSOralPaperAnalysisReport findFlagReport() {
        Find find = Find.find(filterBuilder.where("pid").is(0L)
                .and("schid").is(0L));
        return __find_OTF(find, ReadPreference.primary()).stream().findFirst().orElse(null);
    }

    public void updateFlagReportTime(String id, Date time) {
        // force update createAt
        Update update = updateBuilder.update("ct", time);
        update(id, update);
    }
}
