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
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.dao.mongo.mql.Update;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.business.api.entity.RSPaperAnalysisReport;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

import javax.inject.Named;
import java.util.*;

/**
 * Created by Changyuan on 2015/1/19.
 */
@Named
public class RSPaperAnalysisReportDao extends StaticMongoDao<RSPaperAnalysisReport, String> {
    @Override
    protected void calculateCacheDimensions(RSPaperAnalysisReport source, Collection<String> dimensions) {
    }

    @Override
    protected void preprocessEntity(RSPaperAnalysisReport entity) {
        super.preprocessEntity(entity);
        entity.initializeIfNecessary();
    }

    public RSPaperAnalysisReport save(RSPaperAnalysisReport report) {
        if (report.getId() == null) {
            report.setId(RandomUtils.nextObjectId());
        }
        Date date = new Date();
        if (report.getCreateAt() == null) {
            report.setCreateAt(date);
        }
        report.setUpdateAt(date);
        report.initializeIfNecessary();

        Bson filter = filterFromId(report.getId());
        BsonDocument replacement = transform(report);
        FindOneAndReplaceOptions options = new FindOneAndReplaceOptions().upsert(true)
                .returnDocument(ReturnDocument.AFTER);
        BsonDocument document = createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndReplace(filter, replacement, options);
        return transform(document);
    }

    public void save(Collection<RSPaperAnalysisReport> reports) {
        reports.forEach(this::save);
    }

    public List<RSPaperAnalysisReport> findByPaperIds(Set<String> paperIds) {
        Filter filter = filterBuilder.build();
        filter.and("paperId").in(paperIds);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<RSPaperAnalysisReport> findAreaDataByPaperAndCityCode(String paperId, Integer cityCode) {
        if (cityCode == null) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filter.and("paperId").is(paperId);
        filter.and("ccode").is(cityCode);
        filter.and("schoolId").is(0L);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<RSPaperAnalysisReport> findAreaDataByPaperAndCityCodes(String paperId, Collection<Long> cityCodes) {
        if (CollectionUtils.isEmpty(cityCodes)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filter.and("paperId").is(paperId);
        filter.and("ccode").in(cityCodes);
        filter.and("schoolId").is(0L);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<RSPaperAnalysisReport> findSchoolDataByPaperAndAreaCode(String paperId, Integer areaCode) {
        if (areaCode == null) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filter.and("paperId").is(paperId);
        filter.and("acode").is(areaCode);
        filter.and("schoolId").ne(0L);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<RSPaperAnalysisReport> findSchoolDataByPaperAndAreaCodes(String paperId, Collection<Long> areaCodes) {
        if (CollectionUtils.isEmpty(areaCodes)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filter.and("paperId").is(paperId);
        filter.and("acode").in(areaCodes);
        filter.and("schoolId").ne(0);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public List<RSPaperAnalysisReport> findSchoolDataByPaperAndSchoolIds(String paperId, Collection<Long> schoolIds) {
        if (CollectionUtils.isEmpty(schoolIds)) {
            return Collections.emptyList();
        }
        Filter filter = filterBuilder.build();
        filter.and("paperId").is(paperId);
        filter.and("schoolId").in(schoolIds);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary());
    }

    public void updateStatisticData(String id, int stuNum, int finishNum, int questionNum, int correctNum,
                                    int listeningScore, int writtenScore, List<String> weakPoints, Date curTime) {
        Update update = updateBuilder.update("stuNum", stuNum)
                .set("finishNum", finishNum)
                .set("questionNum", questionNum)
                .set("correctNum", correctNum)
                .set("listeningScore", listeningScore)
                .set("writtenScore", writtenScore)
                .set("weakPoints", weakPoints)
                .set("updateAt", curTime);
        update(id, update);
    }

    public RSPaperAnalysisReport findFlagReport() {
        Filter filter = filterBuilder.build();
        filter.and("paperId").is("0");
        filter.and("acode").is(0);
        filter.and("ccode").is(0);
        filter.and("schoolId").is(0L);
        Find find = Find.find(filter).limit(1);
        return __find_OTF(find, ReadPreference.primary()).stream().findFirst().orElse(null);
    }

    public void updateFlagReportTime(String id, Date time) {
        Bson filter = filterFromId(id);
        Update update = updateBuilder.update("createAt", time);
        createMongoConnection().collection
                .withWriteConcern(WriteConcern.ACKNOWLEDGED)
                .findOneAndUpdate(filter, update.toBsonDocument());
    }
}
