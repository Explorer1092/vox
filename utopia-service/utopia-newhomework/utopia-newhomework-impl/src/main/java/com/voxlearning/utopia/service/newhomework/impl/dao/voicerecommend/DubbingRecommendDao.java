package com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.DubbingRecommend;

import javax.inject.Named;
import java.util.Collection;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/8/24
 * \* Time: 下午5:28
 * \* Description:
 * \
 */
@Named
@CacheBean(type = DubbingRecommend.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class DubbingRecommendDao extends StaticMongoShardPersistence<DubbingRecommend, String> {
    @Override
    protected void calculateCacheDimensions(DubbingRecommend document, Collection<String> dimensions) {
        dimensions.add(DubbingRecommend.ck_id(document.getId()));
    }
}
