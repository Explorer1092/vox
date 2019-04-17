package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mysql.persistence.StaticMySQLPersistence;
import com.voxlearning.utopia.service.vendor.api.entity.VendorUsageStat;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

@Named
@CacheBean(type = VendorUsageStat.class)
public class VendorUsageStatPersistence extends StaticMySQLPersistence<VendorUsageStat, Long> {

    @Override
    protected void calculateCacheDimensions(VendorUsageStat document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    @CacheMethod
    public long loadEffectiveUser(@CacheParameter("ak") String appKey, @CacheParameter("ym") String queryDate) {

        Criteria criteria = Criteria.where("APP_KEY").is(appKey).and("YEAR_MONTH").is(queryDate);
        VendorUsageStat usageStat = query(Query.query(criteria)).stream().findFirst().orElse(null);
        return usageStat == null ? 0 : usageStat.getTotalNum();
    }

}
