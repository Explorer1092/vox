package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = TeachingResourceCollect.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
public class TeachingResourceCollectDao extends AlpsStaticMongoDao<TeachingResourceCollect, String> {

    @Override
    protected void calculateCacheDimensions(TeachingResourceCollect document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public List<TeachingResourceCollect> loadAllByUser(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        return query(new Query(criteria).with(sort));
    }

    public long disable(Long userId, String collectId) {
        Criteria criteria = Criteria.where("_id").is(collectId);
        Update update = new Update();
        update.set("disabled", true);
        long count = executeUpdateOne(createMongoConnection(), criteria, update);

        // 清理 cache
        TeachingResourceCollect document = new TeachingResourceCollect();
        document.setUserId(userId);
        getCache().deletes(Arrays.asList(document.generateCacheDimensions()));

        return count;
    }
}
