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

package com.voxlearning.utopia.service.business.impl.dao;

import com.mongodb.MongoNamespace;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.DynamicMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementData;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Maofeng Lu
 * @since 14-7-16 上午11:27
 */
@Named
public class DailyIncreasementDataDao extends DynamicMongoDao<DailyIncreasementData, String> {

    @Override
    protected void calculateCacheDimensions(DailyIncreasementData source, Collection<String> dimensions) {
    }

    @Override
    protected String calculateDatabase(String template, DailyIncreasementData entity) {
        return null;
    }

    @Override
    protected String calculateCollection(String template, DailyIncreasementData entity) {
        //如果查中学使用daily_increasement_region_data_middle表,否则使用daily_increasement_region_data表
        if (entity.getSchoolLevel() != null && entity.getSchoolLevel() == SchoolLevel.MIDDLE) {
            return StringUtils.formatMessage(template, "_middle");
        }
        return StringUtils.formatMessage(template, "");
    }

    /**
     * 查询某段时间内相关区域的数据,降序排列
     */
    public List<DailyIncreasementData> findDailyIncreasementByRegionCode(Integer start, Integer end, Set<Integer> regionCodeSet) {
        return findDailyIncreasementByRegionCode(start, end, regionCodeSet, SchoolLevel.JUNIOR);
    }

    public List<DailyIncreasementData> findDailyIncreasementByRegionCode(Integer start, Integer end, Set<Integer> regionCodeSet, SchoolLevel schoolLevel) {
        if (CollectionUtils.isEmpty(regionCodeSet)) {
            return Collections.emptyList();
        }

        MongoNamespace mongoNamespace = dynamicMongoNamespace(schoolLevel);
        Filter filter = filterBuilder.where("areacode").in(regionCodeSet)
                .and("date").gte(start).lte(end)
                .and("status").is(1);
        Find find = Find.find(filter).with(new Sort(Sort.Direction.DESC, "date"));
        return __find_OTF(find, null, mongoNamespace);
    }

    /**
     * 查询某段时间内相关区域的数据,降序排列
     */
    public List<DailyIncreasementData> findDailyIncreasementData(Integer start, Integer end, Integer regionCode) {
        Filter filter = filterBuilder.where("areacode").is(regionCode)
                .and("date").gte(start).lte(end)
                .and("status").is(1);
        Find find = Find.find(filter).with(new Sort(Sort.Direction.DESC, "date"));
        MongoNamespace mongoNamespace = dynamicMongoNamespace(SchoolLevel.JUNIOR);
        return __find_OTF(find, null, mongoNamespace);
    }

    /**
     * 查询某段时间内相关学校的数据,降序排列
     */
    public List<DailyIncreasementData> findDailyIncreasementBySchool(Integer start, Integer end, Set<Long> schoolSet) {
        return findDailyIncreasementBySchool(start, end, schoolSet, SchoolLevel.JUNIOR);
    }

    public List<DailyIncreasementData> findDailyIncreasementBySchool(Integer start, Integer end, Set<Long> schoolSet, SchoolLevel schoolLevel) {
        if (CollectionUtils.isEmpty(schoolSet)) {
            return Collections.emptyList();
        }

        MongoNamespace mongoNamespace = dynamicMongoNamespace(schoolLevel);

        Filter filter = filterBuilder.where("school_id").in(schoolSet)
                .and("date").gte(start).lte(end)
                .and("status").is(1);
        Find find = Find.find(filter).with(new Sort(Sort.Direction.DESC, "date"));
        return __find_OTF(find, null, mongoNamespace);
    }

    public List<DailyIncreasementData> findByAreaAndSchools(Integer start, Integer end, Integer county, Collection<Long> schools) {
        MongoNamespace mongoNamespace = dynamicMongoNamespace(SchoolLevel.JUNIOR);
        Filter filter = filterBuilder.where("county").is(county).and("school_id").in(schools).and("date").gte(start).lte(end).and("status").is(1);
        Find find = Find.find(filter).with(new Sort(Sort.Direction.DESC, "date"));
        return __find_OTF(find, null, mongoNamespace);
    }

    private MongoNamespace dynamicMongoNamespace(SchoolLevel schoolLevel) {
        DailyIncreasementData entity = new DailyIncreasementData();
        entity.setSchoolLevel(schoolLevel);

        String d = determineDatabase(null);
        String c = determineCollection(entity);
        return new MongoNamespace(d, c);
    }
}
