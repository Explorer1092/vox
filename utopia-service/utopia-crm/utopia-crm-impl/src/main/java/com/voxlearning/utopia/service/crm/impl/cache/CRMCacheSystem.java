package com.voxlearning.utopia.service.crm.impl.cache;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import javax.inject.Named;

@Named
public class CRMCacheSystem extends SpringContainerSupport {
    public CRMCacheSystem.CBS_Container CBS;

    public CRMCacheSystem() {
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.CBS = new CRMCacheSystem.CBS_Container();
        this.CBS.flushable = CacheSystem.CBS.getCache("flushable");
        this.CBS.unflushable = CacheSystem.CBS.getCache("unflushable");
        this.CBS.persistence = CacheSystem.CBS.getCache("persistence");
    }

    public static class CBS_Container {
        public UtopiaCache flushable;
        public UtopiaCache persistence;
        public UtopiaCache unflushable;

        public CBS_Container() {
        }
    }
}
