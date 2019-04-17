package com.voxlearning.utopia.service.vendor.impl.version;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import javax.inject.Named;
import java.util.Objects;

/**
 * @author jiangpeng
 * @since 2017-06-21 下午12:24
 **/
@Named
public class MySelfStudyGlobalMsgVersion extends SpringContainerSupport {
    private static final String KEY = "MySelfStudyGlobalMsg";

    private UtopiaCache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        cache = CacheSystem.CBS.getCache("storage");
        Objects.requireNonNull(cache);
    }

    public long currentVersion() {
        return SafeConverter.toLong(cache.load(KEY), 1);
    }

    public void increase() {
        cache.incr(KEY, 1, 2, 0);
    }


}
