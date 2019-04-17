package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.Date;

/**
 * @author chensn
 * @date 2018-10-23 15:33
 */
@UtopiaCachePrefix(prefix = "class_zone_activity_rank_like")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed,value = 86400 * 30)
public class ClassRankLikeCountCache extends PojoCacheObject<String, String> {
    protected ClassRankLikeCountCache(UtopiaCache cache) {
        super(cache);
    }
    public void increaseLikeCount(Integer activityId,Integer type,String toObjectId,Boolean daily) {
        String key = cacheKey(generateKey(activityId,type,toObjectId,daily));
        cache.incr(key, 1, 1, expirationInSeconds());
    }
    public String loadLikeCount(Integer activityId,Integer type,String toObjectId,Boolean daily) {
        String key = cacheKey(generateKey(activityId,type,toObjectId,daily));
        return cache.load(key);
    }
    private String generateKey(Integer activityId,Integer type,String toObjectId,Boolean daily){
        if(daily!=null && daily){
            return ":"+activityId+"_"+type+"_"+toObjectId+"_"+DateUtils.dateToString(new Date(),"yyyy-MM-dd");
        }else {
            return ":"+activityId+"_"+type+"_"+toObjectId;
        }
    }
}
