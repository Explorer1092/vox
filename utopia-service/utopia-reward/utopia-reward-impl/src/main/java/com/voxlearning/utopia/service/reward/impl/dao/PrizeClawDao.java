package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.PrizeClaw;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = PrizeClaw.class)
public class PrizeClawDao extends AlpsStaticJdbcDao<PrizeClaw, Long> {
    @Override
    protected void calculateCacheDimensions(PrizeClaw document, Collection<String> dimensions) {
        dimensions.add(PrizeClaw.ck_site(document.getSite()));
    }

    @CacheMethod
    public List<PrizeClaw> loadBySite(@CacheParameter("SITE")int site) {
        Criteria criteria = Criteria.where("SITE").is(site);
        return query(Query.query(criteria));
    }
}
