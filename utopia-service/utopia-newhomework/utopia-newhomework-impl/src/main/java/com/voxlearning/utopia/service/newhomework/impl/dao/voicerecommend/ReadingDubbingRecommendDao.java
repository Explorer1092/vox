package com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ReadingDubbingRecommend;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = ReadingDubbingRecommend.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class ReadingDubbingRecommendDao extends StaticMongoShardPersistence<ReadingDubbingRecommend, String> {
    @Override
    protected void calculateCacheDimensions(ReadingDubbingRecommend document, Collection<String> dimensions) {
        dimensions.add(ReadingDubbingRecommend.ck_id(document.getId()));
    }
}
