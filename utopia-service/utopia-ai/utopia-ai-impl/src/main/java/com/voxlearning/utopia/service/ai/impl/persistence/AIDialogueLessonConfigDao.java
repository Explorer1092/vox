package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.AIDialogueLessonConfig;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = AIDialogueLessonConfig.class)
public class AIDialogueLessonConfigDao extends AlpsStaticMongoDao<AIDialogueLessonConfig, String> {
    @Override
    protected void calculateCacheDimensions(AIDialogueLessonConfig document, Collection<String> dimensions) {
        dimensions.add(AIDialogueLessonConfig.ck_id(document.getId()));
    }

    public List<AIDialogueLessonConfig> findAll() {
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
        AIDialogueLessonConfig config = load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(AIDialogueLessonConfig.ck_id(config.getId()));
            getCache().deletes(cacheIds);
        }
    }
}
