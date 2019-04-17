package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * 测试阿分题视频总购买人数缓存
 * Created by liu jingchao 2017/3/30.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiCourseBuyerCountManager {
    @Inject
    private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;

    @Test
    public void testUpsertBuyerCount() throws Exception {
        assertEquals(109890, asyncAfentiCacheService.getAfentiCourseBuyerCountManager().getCurrentBuyerCount().intValue());
        asyncAfentiCacheService.getAfentiCourseBuyerCountManager().upsertBuyerCount();
        assertEquals(109891, asyncAfentiCacheService.getAfentiCourseBuyerCountManager().getCurrentBuyerCount().intValue());
    }
}