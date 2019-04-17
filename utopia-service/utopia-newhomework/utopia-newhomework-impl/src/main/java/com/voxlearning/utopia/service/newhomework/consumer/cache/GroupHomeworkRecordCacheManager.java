package com.voxlearning.utopia.service.newhomework.consumer.cache;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_month)
@UtopiaCacheRevision("20171221")
public class GroupHomeworkRecordCacheManager extends PojoCacheObject<String, String> {

    public GroupHomeworkRecordCacheManager(UtopiaCache cache) {
        super(cache);
    }


    public String getCacheKey(Long groupId) {
        return new GroupHomeworkRecordCacheManager.CacheKey(groupId).toString();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKey {
        public Long groupId;

        @Override
        public String toString() {
            return "GROUP_HOMEWORK_RECORD" + groupId;
        }
    }
}
