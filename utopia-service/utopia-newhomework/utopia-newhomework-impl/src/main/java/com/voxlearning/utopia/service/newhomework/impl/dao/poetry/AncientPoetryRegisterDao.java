package com.voxlearning.utopia.service.newhomework.impl.dao.poetry;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryRegister;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/21
 */
@Named
@CacheBean(type = AncientPoetryRegister.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class AncientPoetryRegisterDao extends StaticMongoShardPersistence<AncientPoetryRegister, String> {

    @Override
    protected void calculateCacheDimensions(AncientPoetryRegister document, Collection<String> dimensions) {
        dimensions.add(AncientPoetryRegister.ck_id(document.getId()));
        dimensions.add(AncientPoetryRegister.ck_clazzGroupId(document.getClazzGroupId()));
    }

    @CacheMethod
    public Map<Long, List<AncientPoetryRegister>> loadByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds).and("beenCanceled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        Map<Long, List<AncientPoetryRegister>> ret = query(query).stream()
                .collect(Collectors.groupingBy(AncientPoetryRegister::getClazzGroupId));
        // 为空时返回空list，避免空击穿
        return groupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

}
