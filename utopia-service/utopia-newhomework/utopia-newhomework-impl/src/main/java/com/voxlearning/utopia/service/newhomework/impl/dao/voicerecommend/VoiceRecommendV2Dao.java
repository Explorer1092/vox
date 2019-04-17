package com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.VoiceRecommendV2;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = VoiceRecommendV2.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class VoiceRecommendV2Dao extends StaticMongoShardPersistence<VoiceRecommendV2, String> {
    @Override
    protected void calculateCacheDimensions(VoiceRecommendV2 document, Collection<String> dimensions) {
        dimensions.add(VoiceRecommendV2.ck_id(document.getId()));
    }
}
