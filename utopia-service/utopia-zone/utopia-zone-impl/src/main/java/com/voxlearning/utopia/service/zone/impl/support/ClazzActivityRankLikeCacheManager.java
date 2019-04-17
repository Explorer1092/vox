package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

/**
 * @author chensn
 * @date 2018-10-23 16:41
 */
@Named("com.voxlearning.utopia.service.zone.impl.support.ClazzActivityRankLikeCacheManager")
public class ClazzActivityRankLikeCacheManager implements InitializingBean {

    private ClassRankLikeCountCache classRankLikeCountCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        UtopiaCache cacheStorage = CacheSystem.CBS.getCacheBuilder().getCache("storage");
        classRankLikeCountCache = new ClassRankLikeCountCache(cacheStorage);
    }

    public void increaseLikeCount(Integer activityId,Integer type,String toObjectId,Boolean daily) {
        classRankLikeCountCache.increaseLikeCount(activityId,type,toObjectId,daily);
    }
    public String loadLikeCount(Integer activityId,Integer type,String toObjectId,Boolean daily) {
        return classRankLikeCountCache.loadLikeCount(activityId,type,toObjectId,daily);
    }
}


