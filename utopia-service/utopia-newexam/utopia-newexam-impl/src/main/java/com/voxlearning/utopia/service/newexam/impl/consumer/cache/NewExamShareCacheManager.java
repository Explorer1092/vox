package com.voxlearning.utopia.service.newexam.impl.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @Description: 考试(单元检测)分享Cache
 * @author: Mr_VanGogh
 * @date: 2019/3/22 上午11:34
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190322")
public class NewExamShareCacheManager extends PojoCacheObject<String, Integer> {

    public NewExamShareCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(String eid) {
        return new NewExamShareCacheManager.CacheKey(eid).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public String hid;

        @Override
        public String toString() {
            return "NEW_EXAM_" + hid;
        }
    }
}
