package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.PowerPrize;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
@CacheBean(type = PowerPrize.class)
public class PowerPrizeDao extends AlpsStaticJdbcDao<PowerPrize, Long> {
    @Override
    protected void calculateCacheDimensions(PowerPrize document, Collection<String> dimensions) {
        if (document != null) {
            dimensions.add(PowerPrize.ck_level(document.getLevel()));
        }
        dimensions.add(PowerPrize.ck_all());
    }

    public int updateStock(long id) {
        Criteria criteria = Criteria.where("ID").is(id);
        Update update = new Update();
        update.inc("STOCK", -1);
        int affectRows = (int)$update(update,criteria);
        if(affectRows > 0){
            Set<String> keys = new HashSet<>();
            calculateCacheDimensions(this.load(id), keys);
            getCache().delete(keys);
        }
        return affectRows;
    }

    @CacheMethod
    public List<PowerPrize> loadByLevel(@CacheParameter("LEVEL")int level) {
        Criteria criteria = Criteria.where("level").is(level);
        return query(Query.query(criteria));
    }

    @CacheMethod(key = "ALL")
    public List<PowerPrize> loadAll() {
        return query();
    }
}
