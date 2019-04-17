package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathBoostHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named
public class TobbitMathBoostHistoryDao extends AlpsStaticMongoDao<TobbitMathBoostHistory, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathBoostHistory document, Collection<String> dimensions) {
        dimensions.add(TobbitMathBoostHistory.ck_id(document.getId()));
        dimensions.add(TobbitMathBoostHistory.ck_bid(document.getBid()));
    }

    @CacheMethod
    public List<TobbitMathBoostHistory> loadByBoostId(@CacheParameter("BID") String bid, int limit) {
        Criteria criteria = Criteria.where("bid").is(bid);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }

}
