package com.voxlearning.utopia.service.ai.impl.persistence;

import com.mongodb.client.result.UpdateResult;
import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.api.concurrent.UninterruptiblyFuture;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.utopia.service.ai.entity.ChipsVideoBlackList;

import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author guangqing
 * @since 2019/3/25
 */
@Named
@CacheBean(type = ChipsVideoBlackList.class)
public class ChipsVideoBlackListDao extends AsyncStaticMongoPersistence<ChipsVideoBlackList, Long> {
    @Override
    protected void calculateCacheDimensions(ChipsVideoBlackList chipsVideoBlackList, Collection<String> collection) {
        collection.add(ChipsVideoBlackList.ck_id(chipsVideoBlackList.getId()));

    }

    public List<ChipsVideoBlackList> loadAll() {
        Criteria criteria = Criteria.where("DISABLED").is(false);
        return query(Query.query(criteria));
    }

    public void disable(Long userId) {
        Criteria criteria = Criteria.where("id").is(userId).and("DISABLED").is(false);
        Update update = new Update();
        update.set("DISABLED", true);
        UninterruptiblyFuture<UpdateResult> future = $executeUpdateOne(createMongoConnection(), criteria, update);
        try {
            UpdateResult result = future.get();
            if (result != null) {
                cleanCache(userId);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } catch (ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

    private void cleanCache(Long id) {
        Set<String> cacheSet = new HashSet<>();
        cacheSet.add(ChipsVideoBlackList.ck_id(id));
        getCache().delete(cacheSet);
    }


}
