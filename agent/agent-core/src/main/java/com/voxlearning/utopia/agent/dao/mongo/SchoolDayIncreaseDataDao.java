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

package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.KeyGenerator;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.persist.entity.SchoolDayIncreaseData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jia HuanYin
 * @since 2016/2/19
 */
@Named
public class SchoolDayIncreaseDataDao extends StaticMongoDao<SchoolDayIncreaseData, String> {

    @Inject AgentCacheSystem agentCacheSystem;

    @Override
    protected void calculateCacheDimensions(SchoolDayIncreaseData source, Collection<String> dimensions) {
    }

    public List<SchoolDayIncreaseData> findSchoolData(Long schoolId, Collection<Integer> dayRange) {
        List<String> uniqueKeys = new ArrayList<>();
        for (int date : dayRange) {
            String uniqueKey = StringUtils.join(schoolId, "-", date);
            uniqueKeys.add(uniqueKey);
        }
        return new ArrayList<>(loadSchoolData(uniqueKeys).values());
    }

    public List<SchoolDayIncreaseData> findSchoolData(Collection<Long> schools, int date) {
        List<String> uniqueKeys = new ArrayList<>();
        for (Long schoolId : schools) {
            String uniqueKey = StringUtils.join(schoolId, "-", date);
            uniqueKeys.add(uniqueKey);
        }
        return new ArrayList<>(loadSchoolData(uniqueKeys).values());
    }

    private Map<String, SchoolDayIncreaseData> loadSchoolData(Collection<String> uniqueKeys) {
        if (CollectionUtils.isEmpty(uniqueKeys)) {
            return Collections.emptyMap();
        }

        CacheObjectLoader.Loader<String, SchoolDayIncreaseData> loader = agentCacheSystem.CBS.flushable.getCacheObjectLoader()
            .createLoader(new KeyGenerator<String>() {
                @Override
                public String generate(String source) {
                    String[] segments = StringUtils.split(source, "-");
                    Long schoolId = NumberUtils.toLong(segments[0]);
                    Integer day = NumberUtils.toInt(segments[1]);
                    return SchoolDayIncreaseData.ck_sid_day(schoolId, day);
                }
            });

        return loader.loads(uniqueKeys).loadsMissed(this::internalLoads).write(entityCacheExpirationInSeconds()).getResult();
    }

    private Map<String, SchoolDayIncreaseData> internalLoads(Collection<String> uniqueKeys) {
        if (CollectionUtils.isEmpty(uniqueKeys)) {
            return Collections.emptyMap();
        }

        Set<Long> schoolIds = new LinkedHashSet<>();
        Set<Integer> dayRange = new LinkedHashSet<>();
        for (String unique : uniqueKeys) {
            String[] segments = StringUtils.split(unique, "-");
            long schoolId = NumberUtils.toLong(segments[0]);
            Integer day = NumberUtils.toInt(segments[1]);
            schoolIds.add(schoolId);
            dayRange.add(day);
        }

        Filter filter = filterBuilder.where("day").in(dayRange);
        filterIn(filter, "schoolId", schoolIds);

        List<SchoolDayIncreaseData> resultList = find(filter);
        Map<String, SchoolDayIncreaseData> retMap = new LinkedHashMap<>();
        for (SchoolDayIncreaseData item : resultList) {
            String unique = item.toSchoolUnique();
            if (uniqueKeys.contains(unique)) {
                retMap.put(unique, item);
            }
        }
        return retMap;
    }

    public List<SchoolDayIncreaseData> findRegionData(Collection<Integer> regions, Integer date) {
        List<String> uniqueKeys = new ArrayList<>();
        for (Integer regionCode : regions) {
            String uniqueKey = StringUtils.join(regionCode, "-", date);
            uniqueKeys.add(uniqueKey);
        }
        Map<String, List<SchoolDayIncreaseData>> regionDataMap = loadRegionData(uniqueKeys);
        List<SchoolDayIncreaseData> retList = new ArrayList<>();
        regionDataMap.values().forEach(retList::addAll);
        return retList;
    }

    private Map<String, List<SchoolDayIncreaseData>> loadRegionData(Collection<String> uniqueKeys) {
        if (CollectionUtils.isEmpty(uniqueKeys)) {
            return Collections.emptyMap();
        }

        CacheObjectLoader.Loader<String, List<SchoolDayIncreaseData>> loader = agentCacheSystem.CBS.flushable.getCacheObjectLoader()
            .createLoader(new KeyGenerator<String>() {
                @Override
                public String generate(String source) {
                    String[] segments = StringUtils.split(source, "-");
                    Integer regionCode = NumberUtils.toInt(segments[0]);
                    Integer day = NumberUtils.toInt(segments[1]);
                    return SchoolDayIncreaseData.ck_region_day(regionCode, day);
                }
            });

        return loader.loads(uniqueKeys).loadsMissed(this::internalRegionLoads).writeAsList(entityCacheExpirationInSeconds()).getResult();
    }

    private Map<String, List<SchoolDayIncreaseData>> internalRegionLoads(Collection<String> uniqueKeys) {
        if (CollectionUtils.isEmpty(uniqueKeys)) {
            return Collections.emptyMap();
        }

        Set<Integer> regions = new LinkedHashSet<>();
        Set<Integer> dayRange = new LinkedHashSet<>();
        for (String unique : uniqueKeys) {
            String[] segments = StringUtils.split(unique, "-");
            Integer regionCode = NumberUtils.toInt(segments[0]);
            Integer day = NumberUtils.toInt(segments[1]);
            regions.add(regionCode);
            dayRange.add(day);
        }

        Filter filter = filterBuilder.where("day").in(dayRange);
        filterIn(filter, "countyCode", regions);

        List<SchoolDayIncreaseData> resultList = find(filter);
        resultList = resultList.stream().filter(p -> uniqueKeys.contains(p.toRegionUnique())).collect(Collectors.toList());
        return resultList.stream().collect(Collectors.groupingBy(p -> StringUtils.join(p.getCountyCode(), "-", p.getDay()), Collectors.toList()));
    }

    private void filterIn(Filter filter, String key, Collection<?> values) {
        if (values != null) {
            filter.and(key).in(values);
        }
    }

    private List<SchoolDayIncreaseData> find(Filter filter) {
        return __find_OTF(Find.find(filter));
    }
}
