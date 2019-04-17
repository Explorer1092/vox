package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.*;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.mapper.TeachingResourceStatistics;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.impl.support.BusinessCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by haitian.gan on 2017/8/1.
 */
@Named
@CacheBean(type = TeachingResource.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week))
public class TeachingResourceDao extends AlpsStaticMongoDao<TeachingResource, String> {

    @Inject
    private BusinessCacheSystem businessCacheSystem;

    public List<TeachingResource> loadAll() {
        return query();
    }

    public void incrReadCount(String resourceId, Long incr) {
        Criteria criteria = Criteria.where("_id").is(resourceId);
        Update update = new Update();
        update.inc("readCount", incr);
        executeUpdateOne(createMongoConnection(), criteria, update);

        updateCache(resourceId);
    }

    public void incrCollectCount(String resourceId, Long incr) {
        Criteria criteria = Criteria.where("_id").is(resourceId);
        Update update = new Update();
        update.inc("collectCount", incr);
        executeUpdateOne(createMongoConnection(), criteria, update);

        updateCache(resourceId);
    }

    public void incrParticipateNum(String resourceId) {
        Criteria criteria = Criteria.where("_id").is(resourceId);
        Update update = new Update();
        update.inc("participateNum", 1);
        executeUpdateOne(createMongoConnection(), criteria, update);

        updateCache(resourceId);
    }

    public void incrFinishNum(String resourceId) {
        Criteria criteria = Criteria.where("_id").is(resourceId);
        Update update = new Update();
        update.inc("finishNum", 1);
        executeUpdateOne(createMongoConnection(), criteria, update);

        updateCache(resourceId);
    }


    private void updateCache(String resourceId) {
        TeachingResource teachingResource = $load(resourceId);
        String cacheKey = TeachingResource.ck_id(resourceId);
        businessCacheSystem.CBS.flushable.<TeachingResource>createCacheValueModifier()
                .key(cacheKey)
                .expiration(DateUtils.getCurrentToWeekEndSecond())
                .modifier(item -> {
                    item.setReadCount(teachingResource.getReadCount());
                    item.setCollectCount(teachingResource.getCollectCount());
                    item.setFinishNum(teachingResource.getFinishNum());
                    item.setParticipateNum(teachingResource.getParticipateNum());
                    return item;
                })
                .execute();

        cacheKey = TeachingResourceStatistics.ck_id(resourceId);
        businessCacheSystem.CBS.flushable.<TeachingResourceStatistics>createCacheValueModifier()
                .key(cacheKey)
                .expiration(DateUtils.getCurrentToWeekEndSecond())
                .modifier(item -> {
                    item.setReadCount(teachingResource.getReadCount());
                    item.setCollectCount(teachingResource.getCollectCount());
                    item.setFinishNum(teachingResource.getFinishNum());
                    item.setParticipateNum(teachingResource.getParticipateNum());
                    return item;
                })
                .execute();
    }

    @Override
    protected void calculateCacheDimensions(TeachingResource document, Collection<String> dimensions) {
        dimensions.add(TeachingResource.ck_id(document.getId()));
        dimensions.add(TeachingResource.ck_all());
    }
}
