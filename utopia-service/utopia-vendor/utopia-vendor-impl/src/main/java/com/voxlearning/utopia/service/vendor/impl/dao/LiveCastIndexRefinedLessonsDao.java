package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.dao.core.annotation.CacheDimension;
import com.voxlearning.alps.dao.core.annotation.CacheDimensionDistribution;
import com.voxlearning.alps.dao.mongo.persistence.AsyncStaticMongoPersistence;
import com.voxlearning.alps.dao.mongo.support.MongoExceptionUtils;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author jiangpeng
 * @since 2017-10-17 下午5:46
 **/
@Named
@CacheBean(type = LiveCastIndexRefinedLessons.class)
@CacheDimension(CacheDimensionDistribution.ID_FIELD)
public class LiveCastIndexRefinedLessonsDao extends AsyncStaticMongoPersistence<LiveCastIndexRefinedLessons, String> {
    @Override
    protected void calculateCacheDimensions(LiveCastIndexRefinedLessons document, Collection<String> dimensions) {
        dimensions.addAll(Arrays.asList(document.generateCacheDimensions()));
    }


    @Override
    public LiveCastIndexRefinedLessons upsert(LiveCastIndexRefinedLessons document) {
        LiveCastIndexRefinedLessons result = null;

        try {
            result = super.upsert(document);
        }catch (Exception e){
            if (MongoExceptionUtils.isDuplicateKeyError(e))
                super.upsert(document);
            else
                throw e;
        }
        if (result != null) {
            //提高缓存利用率  会有并发问题，业务上不会有同一 id 并发的请求，忽略
            getCache().set(cacheKeyFromId(result.getId()), getDefaultCacheExpirationInSeconds(), result);
        }
        return result;
    }
}
