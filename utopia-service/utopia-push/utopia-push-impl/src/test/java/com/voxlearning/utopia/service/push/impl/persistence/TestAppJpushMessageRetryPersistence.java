/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.push.impl.persistence;

import com.voxlearning.alps.annotation.cache.IgnoreFlushCache;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.push.api.entity.AppJpushMessageRetry;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author shiwe.liao
 * @since 2016/1/18
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@DropMongoDatabase
public class TestAppJpushMessageRetryPersistence {

    @Inject private AppJpushMessageRetryPersistence appJpushMessageRetryPersistence;

    @Test
    public void testLoadRetryList() {
        List<ObjectId> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            AppJpushMessageRetry retry = new AppJpushMessageRetry();
            Date date = new Date();
            retry.setMessageSource("STUDENT");
            retry.setNotify("test");
            retry.setTargetUrl("www.17zuoye.com");
            retry.setRetryCount(0);
            retry.setStatus(0);
            retry.setCreateTime(date.getTime());
            retry.setUpdateTime(date.getTime());
            appJpushMessageRetryPersistence.insert(retry);
            ObjectId id = retry.getId();
            ids.add(id);
        }
        List<AppJpushMessageRetry> retryList = appJpushMessageRetryPersistence.loadRetryList();
        assertEquals(retryList.size(), 10);
        for (AppJpushMessageRetry retry : retryList) {
            assertEquals(retry.getStatus().intValue(), 0);
            assertEquals(retry.getRetryCount().intValue(), 0);
        }
        for (ObjectId id : ids) {
            appJpushMessageRetryPersistence.updateRetryFailed(id, 2);

        }
        List<AppJpushMessageRetry> updateList = appJpushMessageRetryPersistence.loadRetryList();
        assertEquals(updateList.size(), 10);
        for (AppJpushMessageRetry retry : updateList) {
            assertEquals(retry.getStatus().intValue(), 0);
            assertEquals(retry.getRetryCount().intValue(), 2);
        }
    }

    @Test
    public void testUpdateRetryFailed() throws Exception {
        AppJpushMessageRetry inst = new AppJpushMessageRetry();
        inst.setRetryCount(2);
        appJpushMessageRetryPersistence.insert(inst);
        ObjectId id = inst.getId();
        appJpushMessageRetryPersistence.updateRetryFailed(id);
        inst = appJpushMessageRetryPersistence.load(id);
        assertEquals(3, inst.getRetryCount().intValue());
    }
}
