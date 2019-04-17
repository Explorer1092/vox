package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestZoneSignInCacheManager {

    @Inject private ZoneSignInCacheManager zoneSignInCacheManager;

    @Test
    public void testZoneSignInCacheManager() throws Exception {
        long studentId = 30009;
        long clazzId = 34051;
        assertFalse(zoneSignInCacheManager.alreadySignedIn(studentId, clazzId));
        zoneSignInCacheManager.setSignedIn(studentId, clazzId);
        assertTrue(zoneSignInCacheManager.alreadySignedIn(studentId, clazzId));
    }
}
