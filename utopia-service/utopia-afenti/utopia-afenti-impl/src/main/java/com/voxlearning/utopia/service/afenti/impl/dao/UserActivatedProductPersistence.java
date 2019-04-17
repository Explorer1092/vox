package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.alps.lang.util.Sets;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User activated product persistence
 * Created by alex on 2016/12/6.
 */
@Named("afenti.UserActivatedProductPersistence")
@UtopiaCacheSupport(UserActivatedProduct.class)
public class UserActivatedProductPersistence extends AlpsStaticJdbcDao<UserActivatedProduct, String> {

    @Override
    protected void calculateCacheDimensions(UserActivatedProduct document, Collection<String> dimensions) {
        dimensions.add(UserActivatedProduct.ck_userId(document.getUserId()));
    }

    public Map<Long, List<UserActivatedProduct>> loadByUserIds(Collection<Long> userIds) {
        CacheObjectLoader.Loader<Long, List<UserActivatedProduct>> loader = getCache()
                .getCacheObjectLoader()
                .createLoader(UserActivatedProduct::ck_userId);
        return loader.loads(Sets.iterableToSetExcludeNull(userIds))
                .loadsMissed(this::__loadByUserIds)
                .writeAsList(getDefaultCacheExpirationInSeconds())
                .getResult();
    }

    private Map<Long, List<UserActivatedProduct>> __loadByUserIds(Collection<Long> userIds) {
        Criteria criteria = Criteria.where("USER_ID").in(userIds)
                .and("DISABLED").is(false);
        return query(Query.query(criteria)).stream()
                .collect(Collectors.groupingBy(UserActivatedProduct::getUserId));
    }
}
