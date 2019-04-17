package com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.StaticMongoShardPersistence;
import com.voxlearning.utopia.service.newhomework.api.entity.voicerecommend.ImageTextRecommend;

import javax.inject.Named;
import java.util.Collection;

@Named
@CacheBean(type = ImageTextRecommend.class, cacheName = "utopia-homework-cache", useValueWrapper = true)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class ImageTextRecommendDao extends StaticMongoShardPersistence<ImageTextRecommend, String> {
    @Override
    protected void calculateCacheDimensions(ImageTextRecommend document, Collection<String> dimensions) {
        dimensions.add(ImageTextRecommend.ck_id(document.getId()));
    }
}
