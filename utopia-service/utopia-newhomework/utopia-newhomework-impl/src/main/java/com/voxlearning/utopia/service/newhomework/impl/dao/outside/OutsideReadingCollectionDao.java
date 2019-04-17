package com.voxlearning.utopia.service.newhomework.impl.dao.outside;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.outside.OutsideReadingCollection;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author majianxin
 * @version V1.0
 * @date 2018/11/15
 */
@Named
@CacheBean(type = OutsideReadingCollection.class, cacheName = "utopia-homework-cache")
@CacheDimension(CacheDimensionDistribution.ID_AND_OTHER_FIELDS)
public class OutsideReadingCollectionDao extends StaticMongoShardPersistence<OutsideReadingCollection, String> {

    @Override
    protected void calculateCacheDimensions(OutsideReadingCollection document, Collection<String> dimensions) {
        dimensions.add(OutsideReadingCollection.ck_id(document.getId()));
        dimensions.add(OutsideReadingCollection.ck_studentId(document.getStudentId()));
    }

    @CacheMethod
    public Map<Long, List<OutsideReadingCollection>> loadOutsideReadingCollectionByStudentIds(@CacheParameter(value = "studentId", multiple = true) Collection<Long> studentIds) {

        Criteria criteria = Criteria.where("studentId").in(studentIds).and("disabled").is(false);
        Query query = Query.query(criteria);
        Map<Long, List<OutsideReadingCollection>> ret = query(query).stream()
                .collect(Collectors.groupingBy(OutsideReadingCollection::getStudentId));
        // 为空时返回空list，避免空击穿
        return studentIds.stream()
                .collect(Collectors.toMap(e -> e, e -> ret.getOrDefault(e, new LinkedList<>())));
    }

    public Map<String, OutsideReadingCollection> loadOutsideReadingCollectionByStudentId(Long userId){
        Map<Long, List<OutsideReadingCollection>> outsideReadingCollectionMap = loadOutsideReadingCollectionByStudentIds(Collections.singleton(userId));
        List<OutsideReadingCollection> outsideReadingCollections = outsideReadingCollectionMap.get(userId);
        if (CollectionUtils.isEmpty(outsideReadingCollections)) {
            return Collections.emptyMap();
        }
        return outsideReadingCollections.stream().collect(Collectors.toMap(OutsideReadingCollection::getId, o -> o, (o1, o2) -> o1));
    }


}
