package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathAuthUser;
import com.voxlearning.utopia.service.ai.entity.TobbitMathHistory;
import com.voxlearning.utopia.service.ai.entity.TobbitMathShareHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TobbitMathAuthUserDao extends AlpsStaticMongoDao<TobbitMathAuthUser, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathAuthUser document, Collection<String> dimensions) {
        dimensions.add(TobbitMathAuthUser.ck_id(document.getId()));
        dimensions.add(TobbitMathAuthUser.ck_openId(document.getOpenId()));
    }


    @CacheMethod
    public TobbitMathAuthUser loadByOpenId(@CacheParameter("OPENID") String openId) {
        Criteria criteria = Criteria.where("openId").is(openId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<TobbitMathAuthUser> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    public void save(TobbitMathAuthUser po) {
        if (null == po) {
            return;
        }
        if (po.getId() == null) {
            po.setDisabled(false);
        }
        insert(po);
    }
}
