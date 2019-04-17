package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * 跟读 句子单日朗读次数限制
 *
 * @author jiangpeng
 * @since 2017-03-09 下午8:42
 **/
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class FollowReadDayReadCacheManager extends PojoCacheObject<String, String> {
    public FollowReadDayReadCacheManager(UtopiaCache cache) {
        super(cache);
    }

    //跟读句子次数限制
    public Boolean todayIsOverLimit_sentence(Long userId, String picListenId, Long sentenceId){
        CacheObject<Long> objectCacheObject = getCache().get(generateCacheKey(userId, picListenId, sentenceId));
        Long count = SafeConverter.toLong(objectCacheObject.getValue());
        return count >= 10;
    }

    public Long todayReadOne_sentence(Long userId, String picListenId, Long sentenceId){
        return getCache().incr(generateCacheKey(userId, picListenId, sentenceId), 1, 1, expirationInSeconds());
    }

    private String generateCacheKey(Long userId, String picListenId,  Long sentenceId){
        return "FollowReadDayReadCacheManager_" + userId + "_" + picListenId + "_" + sentenceId;
    }

    //跟读作品次数限制
    public Boolean todayIsOVerLimit_collection(Long studentId){
        CacheObject<Long> objectCacheObject = getCache().get("followReadCollectionCount_" + studentId);
        Long count = SafeConverter.toLong(objectCacheObject.getValue());
        return count >=10;
    }
    public Long todayCreateOne_collection(Long studentId){
        return getCache().incr("followReadCollectionCount_" + studentId, 1, 1, expirationInSeconds());
    }


    // 跟读作品点赞
    public Long someOneLikeColletion(String id){
        return getCache().incr("followReadCollectionLike_" + id, 1, 1, 86400 * 30);
    }

    public Long collectionLikeCount(String id){
        CacheObject<Long> objectCacheObject = getCache().get("followReadCollectionLike_" + id);
        return SafeConverter.toLong(objectCacheObject.getValue());
    }

}
