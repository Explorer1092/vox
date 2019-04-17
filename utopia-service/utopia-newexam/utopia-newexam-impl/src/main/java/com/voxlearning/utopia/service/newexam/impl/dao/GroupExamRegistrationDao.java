package com.voxlearning.utopia.service.newexam.impl.dao;

import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newexam.api.entity.GroupExamRegistration;

import javax.inject.Named;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/1/28
 */
@Named
@CacheBean(type = GroupExamRegistration.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class GroupExamRegistrationDao extends StaticMongoShardPersistence<GroupExamRegistration, String> {

    @Override
    protected void calculateCacheDimensions(GroupExamRegistration document, Collection<String> dimensions) {
        dimensions.add(GroupExamRegistration.ck_id(document.getId()));
        dimensions.add(GroupExamRegistration.ck_clazzGroupId(document.getClazzGroupId()));
    }

    public Map<String, GroupExamRegistration> loadsUncancelled(Collection<String> ids) {
       return Maps.filterValues(loads(ids), ger -> ger != null && !ger.getBeenCanceled());
    }

    @CacheMethod
    public Map<Long, List<GroupExamRegistration>> loadByClazzGroupIds(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds).and("beenCanceled").is(Boolean.FALSE);
        Query query = Query.query(criteria);
        Map<Long, List<GroupExamRegistration>> ret = query(query).stream()
                .collect(Collectors.groupingBy(GroupExamRegistration::getClazzGroupId));
        // 为空时返回空list，避免空击穿
        return groupIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

}
