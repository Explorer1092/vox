package com.voxlearning.utopia.service.mizar.consumer.manager;

import com.voxlearning.alps.annotation.cache.UtopiaCachePrefix;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * Created by Summer Yang on 2016/9/19.
 *
 */
@UtopiaCachePrefix(prefix = "MIZAR:COURSE:RC")
public class MizarCourseReadCountManager extends PojoCacheObject<String, String> {
    public MizarCourseReadCountManager(UtopiaCache cache) {
        super(cache);
    }

    public long increaseCount(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return 0;
        }
        String cacheKey = cacheKey(courseId);
        return SafeConverter.toLong(cache.incr(cacheKey, 1, 1, 0));
    }

    public long loadReadCount(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return 0;
        }
        String value = load(courseId);
        return SafeConverter.toLong(value);
    }
}
