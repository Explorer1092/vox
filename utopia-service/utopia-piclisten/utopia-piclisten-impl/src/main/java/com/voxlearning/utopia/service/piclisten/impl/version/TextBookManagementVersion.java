package com.voxlearning.utopia.service.piclisten.impl.version;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import javax.inject.Named;
import java.util.Objects;

/**
 * Created by jiang wei on 2017/4/5.
 */
@Named
public class TextBookManagementVersion extends SpringContainerSupport {
    private static final String KEY = "TextBookManagementVersion";

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
