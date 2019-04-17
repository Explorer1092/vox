package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.AIDialogueTaskConfig;

import javax.inject.Named;
import java.util.*;

/**
　* @Description:
　* @author zhiqi.yao
　* @date 2018/4/12 20:43
*/
@Named
@CacheBean(type = AIDialogueTaskConfig.class)
public class AIDialogueTaskConfigDao extends AlpsStaticMongoDao<AIDialogueTaskConfig, String> {
    @Override
    protected void calculateCacheDimensions(AIDialogueTaskConfig document, Collection<String> dimensions) {
        dimensions.add(AIDialogueTaskConfig.ck_id(document.getId()));
    }

    public List<AIDialogueTaskConfig> findAll() {
        Criteria criteria = Criteria.where("disabled").is(false);
        return query(Query.query(criteria));
    }
    public void deleteById(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().set("disabled", true)
                .set("updateTime", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCacheById(id);
        }
    }

    private void cleanCacheById(String id) {
        AIDialogueTaskConfig config = load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(AIDialogueTaskConfig.ck_id(config.getId()));
            getCache().deletes(cacheIds);
        }
    }
}
