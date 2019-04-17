package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Sort;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserUnitResultHistory;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Named
@CacheBean(type = ChipsWechatUserUnitResultHistory.class)
public class ChipsWechatUserUnitResultHistoryDao extends AsyncStaticMongoPersistence<ChipsWechatUserUnitResultHistory, String> {

    @Override
    protected void calculateCacheDimensions(ChipsWechatUserUnitResultHistory document, Collection<String> dimensions) {
        dimensions.add(ChipsWechatUserUnitResultHistory.ck_userId_unitId(document.getUserId(), document.getUnitId()));
        dimensions.add(ChipsWechatUserUnitResultHistory.ck_uid(document.getUserId()));
    }

    @Override
    public ChipsWechatUserUnitResultHistory load(String s) {
        return super.$load(s).getUninterruptibly();
    }

    @CacheMethod
    public ChipsWechatUserUnitResultHistory load(@CacheParameter("UID") Long user, @CacheParameter("UNIT_ID") String unitId) {
        Criteria criteria = Criteria.where("userId").is(user).and("unitId").is(unitId).and("disabled").is(false);
        Sort sort = new Sort(Sort.Direction.DESC, "createDate");
        Query query = Query.query(criteria).with(sort).limit(1);
        List<ChipsWechatUserUnitResultHistory> list = query(query);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @CacheMethod
    public List<ChipsWechatUserUnitResultHistory> loadByUser(@CacheParameter("UID") Long user) {
        Criteria criteria = Criteria.where("userId").is(user).and("disabled").is(false);
        return query( Query.query(criteria));
    }


    public void disabled(Long userId, String unitId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("unitId").is(unitId).and("disabled").is(false);
        Update update = new Update();
        update.set("disabled", true);
        $executeUpdateMany(createMongoConnection(), criteria, update);
        cleanCache(userId, unitId);
    }




    private void cleanCache(Long userId, String unitId) {
        Set<String> cacheSet = new HashSet<>();
        cacheSet.add(ChipsWechatUserUnitResultHistory.ck_userId_unitId(userId, unitId));
        cacheSet.add(ChipsWechatUserUnitResultHistory.ck_uid(userId));
        getCache().delete(cacheSet);
    }
}
