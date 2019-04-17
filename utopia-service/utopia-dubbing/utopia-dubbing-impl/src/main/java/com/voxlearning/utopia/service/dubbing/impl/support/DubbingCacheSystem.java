package com.voxlearning.utopia.service.dubbing.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-8-24
 */
@Named
public class DubbingCacheSystem extends SpringContainerSupport {

    @Getter
    private DubbingPersistenceCache dubbingPersistenceCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        this.dubbingPersistenceCache = new DubbingPersistenceCache(persistence);
    }
}
