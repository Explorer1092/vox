package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.reward.entity.newversion.LeaveWord;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Named
@CacheBean(type = LeaveWord.class)
public class LeaveWordDao extends AsyncStaticMongoPersistence<LeaveWord, String> {

    @Override
    protected void calculateCacheDimensions(LeaveWord document, Collection<String> dimensions) {
        dimensions.add(LeaveWord.ck_userId(document.getUserId()));
    }

    @CacheMethod
    public List<LeaveWord> loadByUserId(@CacheParameter("USER_ID") Long userId) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return query(Query.query(criteria));
    }

    public void updateToReadAlready(Long userId, Set<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return;
        }
        Criteria criteria = Criteria.where("userId").is(userId).and("leaveWordGoodsId").in(idList);

        Update update = new Update();
        update.set("isRead", true);
        this.$executeUpdateMany(createMongoConnection(), criteria, update);

        String cacheKey = LeaveWord.ck_userId(userId);
        getCache().delete(cacheKey);
    }
}