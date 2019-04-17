package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestUploadPhotoCacheManager {

    @Inject private UploadPhotoCacheManager uploadPhotoCacheManager;

    @Test
    public void testUploadPhotoCacheManager() throws Exception {
        uploadPhotoCacheManager.photoUploaded(30009L, 34051L);
        assertTrue(uploadPhotoCacheManager.alreadyUploaded(30009L, 34051L));
    }
}
