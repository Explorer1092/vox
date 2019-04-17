package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathBoostBill;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TobbitMathBoostBillDao extends AlpsStaticMongoDao<TobbitMathBoostBill, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathBoostBill document, Collection<String> dimensions) {
        dimensions.add(TobbitMathBoostBill.ck_id(document.getId()));
        dimensions.add(TobbitMathBoostBill.ck_uid(document.getUid()));
        dimensions.add(TobbitMathBoostBill.ck_openId(document.getOpenId()));
    }

    @CacheMethod
    public List<TobbitMathBoostBill> loadByUid(@CacheParameter("UID") Long uid, int limit) {
        Criteria criteria = Criteria.where("uid").is(uid);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }


    @CacheMethod
    public List<TobbitMathBoostBill> loadByOpenId(@CacheParameter("OPENID") String openId,int limit) {
        Criteria criteria = Criteria.where("openId").is(openId);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }

}
