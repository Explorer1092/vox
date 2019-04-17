package com.voxlearning.utopia.service.newhomework.impl.dao.outside;


import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReading;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@CacheBean(type = OutsideReading.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class OutsideReadingDao extends StaticMongoShardPersistence<OutsideReading, String> {

    @Override
    protected void calculateCacheDimensions(OutsideReading document, Collection<String> dimensions) {
        dimensions.add(OutsideReading.ck_id(document.getId()));
        dimensions.add(OutsideReading.ck_clazzGroupId(document.getClazzGroupId()));
    }

    @CacheMethod
    public Map<Long, List<OutsideReading>> loadOutsideReadingByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {

        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds).and("disabled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        Map<Long, List<OutsideReading>> ret = query(query).stream()
                .collect(Collectors.groupingBy(OutsideReading::getClazzGroupId));
        // 为空时返回空list，避免空击穿
        return groupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

}
