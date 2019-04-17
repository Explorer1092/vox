package com.voxlearning.utopia.service.newhomework.impl.dao.poetry;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.dao.core.hql.Query;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMission;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/21
 */
@Named
@CacheBean(type = AncientPoetryMission.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class AncientPoetryMissionDao extends StaticMongoShardPersistence<AncientPoetryMission, String> {

    @Override
    protected void calculateCacheDimensions(AncientPoetryMission document, Collection<String> dimensions) {
        dimensions.add(AncientPoetryMission.ck_id(document.getId()));
    }

    /**
     * CRM
     * 获取所有的活动
     * @return
     */
    public List<AncientPoetryMission> loadAllPoetry() {
        Query query = Query.query(new Criteria());
        return query(query);
    }
}
