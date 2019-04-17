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

package com.voxlearning.utopia.service.business.impl.dao.summerreporter;

import com.mongodb.ReadPreference;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.alps.dao.mongo.mql.Filter;
import com.voxlearning.alps.dao.mongo.mql.Find;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCollectSchool;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by jiangpeng on 16/6/13.
 */
@Named
@UtopiaCacheSupport(StudentCollectSchool.class)
public class StudentCollectSchoolDao extends StaticMongoDao<StudentCollectSchool, String> {

    @Override
    protected void calculateCacheDimensions(StudentCollectSchool source, Collection<String> dimensions) {
        dimensions.add(StudentCollectSchool.ck_id(source.getId()));
        dimensions.add(StudentCollectSchool.ck_studentId(source.getStudentId()));
        dimensions.add(StudentCollectSchool.ck_countyId(source.getCountyId()));
    }


    public Map<Long,List<StudentCollectSchool>> findByStudentIds(Collection<Long> studentIds){
        CacheObjectLoader.Loader<Long,List<StudentCollectSchool>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(StudentCollectSchool::ck_studentId);
        return loader.loads(CollectionUtils.toLinkedHashSet(studentIds))
                .loadsMissed(this::__findByStudentIds)
                .writeAsList(entityCacheExpirationInSeconds())
                .getResult();
    }

    public Map<Integer,List<StudentCollectSchool>> findByCountyIds(Collection<Integer> countyIds){
        CacheObjectLoader.Loader<Integer,List<StudentCollectSchool>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(StudentCollectSchool::ck_countyId);
        return loader.loads(CollectionUtils.toLinkedHashSet(countyIds))
                .loadsMissed(this::__findByCountyIds)
                .writeAsList(entityCacheExpirationInSeconds())
                .getResult();
    }




    private Map<Long, List<StudentCollectSchool>> __findByStudentIds(Collection<Long> studentIds) {
        Filter filter = filterBuilder.where("student_id").in(studentIds);
        Sort sort =  new Sort(Sort.Direction.DESC,"ct");
        Find find = Find.find(filter).with(sort);
        return __find_OTF(find, ReadPreference.primary())
                .stream()
                .collect(Collectors.groupingBy(StudentCollectSchool::getStudentId));
    }

    private Map<Integer, List<StudentCollectSchool>> __findByCountyIds(Collection<Integer> countyIds) {
        Filter filter = filterBuilder.where("county_id").in(countyIds);
        Find find = Find.find(filter);
        return __find_OTF(find, ReadPreference.primary())
                .stream()
                .collect(Collectors.groupingBy(StudentCollectSchool::getCountyId));
    }
}
