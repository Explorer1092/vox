package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;

import javax.inject.Named;
import java.util.*;

/**
 * Created by Summer on 2018/3/26
 */
@Named
@CacheBean(type = AIUserUnitResultHistory.class)
public class AIUserUnitResultHistoryDao extends AsyncStaticMongoPersistence<AIUserUnitResultHistory, String> {

    @Override
    protected void calculateCacheDimensions(AIUserUnitResultHistory document, Collection<String> dimensions) {
        dimensions.add(AIUserUnitResultHistory.ck_id(document.getId()));
        dimensions.add(AIUserUnitResultHistory.ck_uid(document.getUserId()));
        dimensions.add(AIUserUnitResultHistory.ck_unit_id(document.getUnitId()));
        dimensions.add(AIUserUnitResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
    }

    public Map<String, AIUserUnitResultHistory> loadByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return loads(ids);
    }


    @Override
    @Deprecated
    public AIUserUnitResultHistory load(String s) {
        return super.load(s);
    }

    @CacheMethod
    public List<AIUserUnitResultHistory> loadByUserId(@CacheParameter("UID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("disabled").is(false);
        return query(Query.query(criteria));
    }

    @CacheMethod
    public List<AIUserUnitResultHistory> loadByUnitId(@CacheParameter("UNID") String unitId) {
        Criteria criteria = Criteria.where("unitId").is(unitId).and("disabled").is(false);
        return query(Query.query(criteria));
    }


    @CacheMethod
    public AIUserUnitResultHistory load(@CacheParameter("UID") Long userId, @CacheParameter("UNIT_ID")String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<AIUserUnitResultHistory> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<AIUserUnitResultHistory> load(Collection<Long> userIdList, String unitId) {
        Criteria criteria = Criteria.where("userId").in(userIdList).and("unitId").is(unitId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        return query(Query.query(criteria).with(sort));
    }

    public void disableOld(Long userId, String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);
        $executeUpdateMany(createMongoConnection(), criteria, update);
        cleanCache(userId, unitId);

    }

    private void cleanCache(Long userId, String unitId) {
        AIUserUnitResultHistory document = load(userId, unitId);
        if (document == null) {
            return;
        }
        Set<String> cacheIds = new HashSet<>();
        cacheIds.add(AIUserUnitResultHistory.ck_id(document.getId()));
        cacheIds.add(AIUserUnitResultHistory.ck_uid(document.getUserId()));
        cacheIds.add(AIUserUnitResultHistory.ck_unit_id(document.getUnitId()));
        cacheIds.add(AIUserUnitResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
        getCache().deletes(cacheIds);

    }
}
