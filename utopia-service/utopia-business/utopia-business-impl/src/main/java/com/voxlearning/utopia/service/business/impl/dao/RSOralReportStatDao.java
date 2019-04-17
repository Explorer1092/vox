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

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.voxlearning.alps.dao.mongo.dao.RangeableMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.utopia.service.business.api.entity.RSOralReportStat;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by tanguohong on 2015/12/1.
 */
@Named
public class RSOralReportStatDao extends RangeableMongoDao<RSOralReportStat>{
    @Override
    protected void calculateCacheDimensions(RSOralReportStat source, Collection<String> dimensions) {
    }

    public List<RSOralReportStat> findExcludeSchool(String taskId, String docId, Integer acode, Long schoolId, Date searchDate){
        MongoNamespace namespace = generateMongoNamespace(generateId(searchDate.getTime()));
        Find find = Find.find(filterBuilder.where("taskId").is(taskId).and("docId").is(docId).and("acode").is(acode).and("schoolId").ne(schoolId));
        return __find_OTF(find, ReadPreference.primary(), namespace);
    }

    public List<RSOralReportStat> findAreaReport(String taskId, String docId, Integer ccode, Long schoolId, Date searchDate){
        MongoNamespace namespace = generateMongoNamespace(generateId(searchDate.getTime()));
        Find find = Find.find(filterBuilder.where("taskId").is(taskId).and("docId").is(docId).and("ccode").is(ccode).and("schoolId").is(schoolId));
        return __find_OTF(find, ReadPreference.primary(), namespace);
    }
}
