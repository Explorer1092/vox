package com.voxlearning.washington.controller.mobile.student.headline.helper;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 3600)
@UtopiaCacheRevision("V2")
@UtopiaCachePrefix(prefix = "STUDENT_APP_HEADLINE_MAPPER")
public class HeadlineMapperCacheManager extends PojoCacheObject<String, StudentHeadlineMapper> {

    public HeadlineMapperCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public String getCacheKey(int typeId, long cjId) {
        return typeId + "::" + cjId;
    }

}