package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.TobbitMathHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
public class TobbitMathHistoryDao extends AlpsStaticMongoDao<TobbitMathHistory, String> {

    @Override
    protected void calculateCacheDimensions(TobbitMathHistory document, Collection<String> dimensions) {
        dimensions.add(TobbitMathHistory.ck_id(document.getId()));
        dimensions.add(TobbitMathHistory.ck_openId(document.getOpenId()));
        dimensions.add(TobbitMathHistory.ck_uid(document.getUid()));
    }


    @CacheMethod
    public List<TobbitMathHistory> loadByUid(@CacheParameter("UID") Long uid, int limit) {
        Criteria criteria = Criteria.where("uid").is(uid).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }

    @CacheMethod
    public List<TobbitMathHistory> loadByOpenId(@CacheParameter("OPENID") String openId, int limit) {
        Criteria criteria = Criteria.where("openId").is(openId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }

    @CacheMethod
    public List<TobbitMathHistory> loadByOpenIdNoUid(@CacheParameter("OPENID_NO_UID") String openId, int limit) {
        Criteria criteria = Criteria.where("openId").is(openId).and("disabled").is(false).and("uid").is(null);
        Sort sort = new Sort(Sort.Direction.DESC, "createTime");
        Query query = Query.query(criteria).with(sort).limit(limit);
        return query(query);
    }


    public long count(String openId, Long uid, boolean includeDisabled) {

        // No uid
        Criteria criteria = Criteria.where("openId").is(openId).and("uid").is(null);
        if (!includeDisabled) {
            criteria.and("disabled").is(false);
        }
        long count = count(Query.query(criteria));

        // Include uid
        criteria = Criteria.where("uid").is(uid);
        if (!includeDisabled) {
            criteria.and("disabled").is(false);
        }
        count += count(Query.query(criteria));
        return count;
    }


    public void save(TobbitMathHistory po) {
        if (null == po) {
            return;
        }
        if (po.getId() == null) {
            po.setDisabled(false);
        }
        insert(po);
    }

    public void disable(String openId, Long uid) {
        Criteria criteria = Criteria.where("openId").is(openId).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);
        updateMany(createMongoConnection(), criteria, update);
        cleanCache(openId, uid);
    }


    private void cleanCache(String openId, Long uid) {
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(TobbitMathHistory.ck_uid(uid));
        cacheIds.add(TobbitMathHistory.ck_openId(openId));
        getCache().deletes(cacheIds);
    }


}
