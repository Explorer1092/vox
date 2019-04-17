package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.newversion.LeaveWordGoods;
import com.voxlearning.utopia.service.reward.entity.newversion.PrizeClaw;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = LeaveWordGoods.class)
public class LeaveWordGoodsDao extends AlpsStaticJdbcDao<LeaveWordGoods, Long> {
    @Override
    protected void calculateCacheDimensions(LeaveWordGoods document, Collection<String> dimensions) {
        dimensions.add(LeaveWordGoods.ck_all());
    }

    @CacheMethod(key = "ALL")
    public List<LeaveWordGoods> loadAll() {
        return query();
    }
}
