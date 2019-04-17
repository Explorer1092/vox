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
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.utopia.service.business.api.entity.BizMarketingSchoolData;
import org.bson.BsonDocument;
import org.bson.BsonInt32;

import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class BizMarketingSchoolDataDao extends StaticMongoDao<BizMarketingSchoolData, String>{
    @Override
    protected void calculateCacheDimensions(BizMarketingSchoolData source, Collection<String> dimensions) {

    }

    public void deleteByCreateDate(Date date) {
        List<String> ids = __find_OTF(filterBuilder.build().and("ts").is(date).toBsonDocument()).stream().map(e -> e.getId()).collect(Collectors.toList()) ;
        __deletes_OTF(ids);
    }

    private Filter getRegionFilter(Integer code, Integer regionType) {
        Filter filterRegion = filterBuilder.build();
        switch (regionType) {
            case 1:
                return filterRegion.and("province_code").is(code);
            case 2:
                return filterRegion.and("city_code").is(code);
            case 3:
                return filterRegion.and("area_code").is(code);
            default:
                break;
        }
        return filterRegion;
    }

    public List<BizMarketingSchoolData> findByDateAndRegionId(Subject subject, String date, Integer code, Integer regionType) {
        Date ts = DateUtils.stringToDate(date, DateUtils.FORMAT_SQL_DATETIME);
        if (StringUtils.isBlank(date) || code == null || ts == null) {
            return Collections.emptyList();
        }
        int tsInt = (int) (ts.getTime() /1000);

        Filter authFilter = filterBuilder.build().orOperator(filterBuilder.build().and("restaff_auth_total").gt(0),
                filterBuilder.build().and("authentication_state").is(AuthenticationState.SUCCESS.getState()));


        Filter filterFinal = filterBuilder.build();
        filterFinal.andOperator(authFilter, getRegionFilter(code, regionType).and("ts").is(tsInt).and("subject").is(subject.name()));

        BsonDocument sort = new BsonDocument();
        sort.put("school_id", new BsonInt32(-1));
        return __find_OTF(filterFinal.toBsonDocument(), null, null, null, sort, ReadPreference.primary());
    }


    public List<BizMarketingSchoolData> findByDateAndRegionId(Subject subject, String startDate, String endDate, Integer code, Integer regionType) {
        Date tsStartDate = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATETIME);
        Date tsEndDate = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATETIME);
        if (StringUtils.isBlank(startDate) || code == null || StringUtils.isBlank(endDate) || tsStartDate == null || tsEndDate == null) {
            return Collections.emptyList();
        }
        int startTs = (int) (tsStartDate.getTime() /1000);
        int endTs = (int) (tsEndDate.getTime() /1000);

        Filter authFilter = filterBuilder.build().orOperator(filterBuilder.build().and("restaff_auth_total").gt(0),
                filterBuilder.build().and("authentication_state").is(AuthenticationState.SUCCESS.getState()));

        Filter filterFinal = filterBuilder.build();
        filterFinal.andOperator(authFilter, getRegionFilter(code, regionType).and("ts").gte(startTs).lte(endTs).and("subject").is(subject.name()));

        BsonDocument sort = new BsonDocument();
        sort.put("school_id", new BsonInt32(-1));
        return __find_OTF(filterFinal.toBsonDocument(), null, null, null, sort, ReadPreference.primary());
    }

    private Filter getRegionFilterTree(Integer code, Integer regionType, List<Integer> areaCodeList) {
        Filter filterRegion = filterBuilder.build();
        switch (regionType) {
            case 1:
                filterRegion.orOperator(filterBuilder.build().and("province_code").is(code),
                        filterBuilder.build().and("area_code").in(areaCodeList));
                return filterRegion;
            case 2:
                filterRegion.orOperator(filterBuilder.build().and("city_code").is(code),
                        filterBuilder.build().and("area_code").in(areaCodeList));
                return filterRegion;
            case 3:
                return filterRegion.and("area_code").is(code);
            default:
                break;
        }
        return filterRegion;
    }


    public List<BizMarketingSchoolData> findByDateAndRegionIds(Subject subject, String startDate, String endDate, Integer code, Integer regionType, List<Integer> areaCodeList, Long schoolId) {
        Date tsStartDate = DateUtils.stringToDate(startDate, DateUtils.FORMAT_SQL_DATETIME);
        Date tsEndDate = DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATETIME);
        if (StringUtils.isBlank(startDate) || code == null || StringUtils.isBlank(endDate) || tsStartDate == null || tsEndDate == null) {
            return Collections.emptyList();
        }
        int startTs = (int) (tsStartDate.getTime() /1000);
        int endTs = (int) (tsEndDate.getTime() /1000);

        Filter authFilter = filterBuilder.build().orOperator(filterBuilder.build().and("restaff_auth_total").gt(0),
                filterBuilder.build().and("authentication_state").is(AuthenticationState.SUCCESS.getState()));

        Filter filterFinal = filterBuilder.build();
        if (schoolId == null || schoolId == 0) {
            filterFinal.andOperator(authFilter, getRegionFilterTree(code, regionType, areaCodeList).and("ts").gte(startTs).lte(endTs).and("subject").is(subject.name()));
        }  else {
            filterFinal.andOperator(authFilter, getRegionFilterTree(code, regionType, areaCodeList).and("school_id").is(schoolId).and("ts").gte(startTs).lte(endTs).and("subject").is(subject.name()));
        }
        BsonDocument sort = new BsonDocument();
        sort.put("ts", new BsonInt32(1));
        return __find_OTF(filterFinal.toBsonDocument(), null, null, null, sort, ReadPreference.primary());
    }
}
