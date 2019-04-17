package com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.dao.AlpsStaticMongoDao;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.VoiceRecommendV1;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = VoiceRecommendV1.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class VoiceRecommendV1Dao extends AlpsStaticMongoDao<VoiceRecommendV1, String> {
    @Override
    protected void calculateCacheDimensions(VoiceRecommendV1 document, Collection<String> dimensions) {
        dimensions.add(VoiceRecommendV1.ck_id(document.getId()));
    }
}
