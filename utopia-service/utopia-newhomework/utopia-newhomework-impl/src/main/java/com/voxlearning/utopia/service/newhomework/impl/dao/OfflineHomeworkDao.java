package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/9/7
 */
@Named
@UtopiaCacheSupport(value = OfflineHomework.class)
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class OfflineHomeworkDao extends AlpsStaticMongoDao<OfflineHomework, String> {
    @Override
    protected void calculateCacheDimensions(OfflineHomework document, Collection<String> dimensions) {
        dimensions.add(OfflineHomework.ck_id(document.getId()));
        dimensions.add(OfflineHomework.ck_newHomeworkId(document.getNewHomeworkId()));
        dimensions.add(OfflineHomework.ck_clazzGroupId(document.getClazzGroupId()));
    }

    @CacheMethod
    public Map<String, OfflineHomework> loadByNewHomeworkIds(@CacheParameter(value = "NHID", multiple = true) Collection<String> newHomeworkIds) {
        Criteria criteria = Criteria.where("newHomeworkId").in(newHomeworkIds);
        Query query = Query.query(criteria);
        return query(query).stream()
                .collect(Collectors.toMap(OfflineHomework::getNewHomeworkId, Function.identity()));
    }

    @CacheMethod
    public Map<Long, List<OfflineHomework>> loadGroupOfflineHomeworks(@CacheParameter(value = "CG", multiple = true) Collection<Long> groupIds) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds);
        Query query = Query.query(criteria);
        return query(query).stream()
                .collect(Collectors.groupingBy(OfflineHomework::getClazzGroupId));
    }

    @Deprecated
    public Page<OfflineHomework> loadGroupOfflineHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable) {
        Criteria criteria = Criteria.where("clazzGroupId").in(groupIds);
        criteria.and("createAt").gte(startDate).lte(endDate);
        Query query = Query.query(criteria);
        return new PageImpl<>(query(query.with(new Sort(Sort.Direction.DESC, "createAt")).with(pageable)), pageable, count(query));
    }
}
