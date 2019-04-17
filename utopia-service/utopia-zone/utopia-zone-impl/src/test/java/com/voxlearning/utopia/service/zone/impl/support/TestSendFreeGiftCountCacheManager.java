package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestSendFreeGiftCountCacheManager {

    @Inject private SendFreeGiftCountCacheManager sendFreeGiftCountCacheManager;

    @Test
    public void testSendFreeGiftCountCacheManager() throws Exception {
        assertEquals(0, sendFreeGiftCountCacheManager.currentCount(30009L));
        sendFreeGiftCountCacheManager.increase(30009L);
        assertEquals(1, sendFreeGiftCountCacheManager.currentCount(30009L));
        sendFreeGiftCountCacheManager.increase(30009L);
        assertEquals(2, sendFreeGiftCountCacheManager.currentCount(30009L));
    }
}
