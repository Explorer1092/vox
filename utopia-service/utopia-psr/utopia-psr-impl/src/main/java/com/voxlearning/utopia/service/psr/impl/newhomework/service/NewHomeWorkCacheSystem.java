package com.voxlearning.utopia.service.psr.impl.newhomework.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import javax.inject.Named;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/8/19
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
@Named
public class NewHomeWorkCacheSystem extends SpringContainerSupport {

    public CBS_Container CBS;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CBS = new CBS_Container();
        CBS.newHomeWork = CacheSystem.CBS.getCache("psr");
    }

    public static class CBS_Container {
        public UtopiaCache newHomeWork;
    }
}
