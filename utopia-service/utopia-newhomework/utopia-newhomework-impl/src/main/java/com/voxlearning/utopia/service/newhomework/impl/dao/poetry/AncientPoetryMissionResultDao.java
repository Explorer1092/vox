package com.voxlearning.utopia.service.newhomework.impl.dao.poetry;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.poetry.AncientPoetryMissionResult;

import javax.inject.Named;
import java.util.Collection;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/2/21
 */
@Named
@CacheBean(type = AncientPoetryMissionResult.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class AncientPoetryMissionResultDao extends StaticMongoShardPersistence<AncientPoetryMissionResult, String> {

    @Override
    protected void calculateCacheDimensions(AncientPoetryMissionResult document, Collection<String> dimensions) {
        dimensions.add(AncientPoetryMissionResult.ck_id(document.getId()));
    }
}
