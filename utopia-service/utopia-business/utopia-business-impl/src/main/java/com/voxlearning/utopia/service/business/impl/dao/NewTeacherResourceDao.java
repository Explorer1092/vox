package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Update;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.alps.spi.cache.CacheValueModifierExecutor;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.impl.dao.buffer.version.NewTeacherResourceWrapperVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named
@CacheBean(type = NewTeacherResource.class)
public class NewTeacherResourceDao extends AlpsStaticMongoDao<NewTeacherResource, String> {

    @Inject
    private NewTeacherResourceWrapperVersion teacherResourceBufferVersion;

    @Override
    protected void calculateCacheDimensions(NewTeacherResource document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }

    public List<NewTeacherResource> loadAll() {
        return query();
    }

    public void deleteAll() {
        for (NewTeacherResource newTeacherResource : loadAll()) {
            if (newTeacherResource.getSource().equals(0)) {
                super.remove(newTeacherResource.getId());
            }
        }
        teacherResourceBufferVersion.increment();
    }

    public void incrReadCount(String id, Long incr) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().inc("readCount", incr);
        long line = executeUpdateOne(createMongoConnection(), criteria, update);
        overrideCache(id, line);
    }

    public void incrCollectCount(String id, Long incr) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().inc("collectCount", incr);
        long line = executeUpdateOne(createMongoConnection(), criteria, update);
        overrideCache(id, line);
    }

    public void incrParticipateNum(String id, Long incr) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().inc("participateNum", incr);
        long line = executeUpdateOne(createMongoConnection(), criteria, update);
        overrideCache(id, line);
    }

    public void incrFinishNum(String id, Long incr) {
        Criteria criteria = Criteria.where("_id").is(id);
        Update update = new Update().inc("finishNum", incr);
        long line = executeUpdateOne(createMongoConnection(), criteria, update);
        overrideCache(id, line);
    }

    /**
     * 暂时这样, 如果量大,可以不更新库, 只更新缓存, 定时把缓存同步到 DB
     *
     * @param id
     * @param updateLine
     */
    private void overrideCache(String id, long updateLine) {
        if (updateLine > 0) {
            NewTeacherResource newTeacherResource = $load(id);
            for (String cacheDimension : newTeacherResource.generateCacheDimensions()) {
                CacheValueModifierExecutor<NewTeacherResource> executor = getCache().createCacheValueModifier();
                executor.key(cacheDimension)
                        .expiration(getDefaultCacheExpirationInSeconds())
                        .modifier(currentValue -> {
                            currentValue.setReadCount(newTeacherResource.getReadCount());
                            return null;
                        }).execute();
            }
        }
    }
}
