package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathAddress;
import com.voxlearning.utopia.service.ai.entity.TobbitMathScoreHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TobbitMathAddressDao extends AlpsStaticMongoDao<TobbitMathAddress, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathAddress document, Collection<String> dimensions) {
        dimensions.add(TobbitMathAddress.ck_id(document.getId()));
        dimensions.add(TobbitMathAddress.ck_uid(document.getUid()));
        dimensions.add(TobbitMathAddress.ck_openId(document.getOpenId()));
    }

    @CacheMethod
    public List<TobbitMathAddress> loadByUid(@CacheParameter("UID") Long uid, int limit) {
        Criteria criteria = Criteria.where("uid").is(uid).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }


    @CacheMethod
    public List<TobbitMathAddress> loadByOpenId(@CacheParameter("OPENID") String openId,int limit) {
        Criteria criteria = Criteria.where("openId").is(openId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }

    public void save(TobbitMathAddress po) {
        if (null == po) {
            return;
        }
        if (po.getId() == null) {
            po.setDisabled(false);
        }
        insert(po);
    }

}
