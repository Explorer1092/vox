package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.service.reward.entity.RewardCoupon;

import javax.inject.Named;
import java.util.Collection;

/**
 * 奖品中心 - 兑换券的DAO实现
 * Created by haitian.gan on 2017/7/20.
 */
@Named
public class RewardCouponDao extends AlpsStaticJdbcDao<RewardCoupon,Long> {

    public RewardCoupon loadRewardCouponByPID(Long productId){
        Criteria criteria = Criteria.where("PRODUCT_ID").is(productId);
        return query(Query.query(criteria)).stream().findFirst().orElse(null);
    }

    @Override
    protected void calculateCacheDimensions(RewardCoupon document, Collection<String> dimensions) {

    }
}
