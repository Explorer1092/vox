package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathScoreHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Named
public class TobbitMathScoreHistoryDao extends AlpsStaticMongoDao<TobbitMathScoreHistory, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathScoreHistory document, Collection<String> dimensions) {
        dimensions.add(TobbitMathScoreHistory.ck_id(document.getId()));
        dimensions.add(TobbitMathScoreHistory.ck_uid(document.getUid()));
        dimensions.add(TobbitMathScoreHistory.ck_cid(document.getCid()));
    }

    @CacheMethod
    public List<TobbitMathScoreHistory> loadByUid(@CacheParameter("UID") Long uid, int limit) {
        Criteria criteria = Criteria.where("uid").is(uid).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }


    public List<TobbitMathScoreHistory> loadByUidCids(Long uid, Set<String> cids) {
        Criteria criteria = Criteria.where("uid").is(uid).and("cid").in(cids).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort);
        return query(query);
    }


    public void save(TobbitMathScoreHistory po) {
        if (null == po) {
            return;
        }
        if (po.getId() == null) {
            po.setDisabled(false);
        }
        insert(po);
    }

}
