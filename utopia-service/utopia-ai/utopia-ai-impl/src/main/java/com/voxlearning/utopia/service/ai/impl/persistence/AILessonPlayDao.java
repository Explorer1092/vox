package com.voxlearning.utopia.service.ai.impl.persistence;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.ai.entity.AIDialogueLessonConfig;
import com.voxlearning.utopia.service.ai.entity.AILessonPlay;

import javax.inject.Named;
import java.util.*;


@Named
@CacheBean(type = AILessonPlay.class)
public class AILessonPlayDao extends AlpsStaticMongoDao<AILessonPlay, String> {
    @Override
    protected void calculateCacheDimensions(AILessonPlay document, Collection<String> dimensions) {
        dimensions.add(AILessonPlay.ck_id(document.getId()));
    }

    public List<AILessonPlay> findAll() {
        Criteria criteria = Criteria.where("disabled").is(false);
        return query(Query.query(criteria));
    }

    public void deleteById(String id) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().set("disabled", true)
                .set("updateDate", new Date());
        long count = executeUpdateOne(createMongoConnection(), criteria, update);
        if (count > 0) {
            cleanCacheById(id);
        }
    }

    private void cleanCacheById(String id) {
        AILessonPlay config = load(id);
        if (config != null) {
            Set<String> cacheIds = new HashSet<>();
            cacheIds.add(AIDialogueLessonConfig.ck_id(config.getId()));
            getCache().deletes(cacheIds);
        }
    }
}
