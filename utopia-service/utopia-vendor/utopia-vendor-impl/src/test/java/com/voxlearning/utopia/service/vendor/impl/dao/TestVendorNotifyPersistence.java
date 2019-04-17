/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.entity.VendorNotify;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = VendorNotify.class)
public class TestVendorNotifyPersistence {

    @Inject private VendorNotifyPersistence vendorNotifyPersistence;

    @Test
    public void testFindUndeliveriedNotify() throws Exception {
        for (int i = 0; i < 5; i++) {
            VendorNotify notify = VendorNotify.mockInstance();
            notify.setStatus(0);
            notify.setRetryCount(RandomUtils.nextInt(1, 3));
            vendorNotifyPersistence.insert(notify);
        }
        assertEquals(5, vendorNotifyPersistence.findUndeliveriedNotify().size());
    }

    @Test
    public void testFindTodayDeliveryFailedNotify() throws Exception {
        for (int i = 0; i < 5; i++) {
            VendorNotify notify = VendorNotify.mockInstance();
            notify.setStatus(0);
            notify.setRetryCount(4);
            notify.setAppKey("");
            vendorNotifyPersistence.insert(notify);
        }
        assertEquals(5, vendorNotifyPersistence.findTodayDeliveryFailedNotify().size());
        assertEquals(5, vendorNotifyPersistence.findTodayDeliveryFailedNotifyCount());
    }

    @Test
    public void testUpdateNotifyDeliveried() throws Exception {
        VendorNotify notify = VendorNotify.mockInstance();
        vendorNotifyPersistence.insert(notify);
        Long id = notify.getId();
        assertEquals(0, vendorNotifyPersistence.load(id).getStatus().intValue());
        assertEquals(1, vendorNotifyPersistence.updateNotifyDeliveried(id));
        assertEquals(1, vendorNotifyPersistence.load(id).getStatus().intValue());
    }

    @Test
    public void testUpdateNotifyDeliveryFailed() throws Exception {
        VendorNotify notify = VendorNotify.mockInstance();
        notify.setRetryCount(0);
        vendorNotifyPersistence.insert(notify);
        Long id = notify.getId();
        vendorNotifyPersistence.updateNotifyDeliveryFailed(id);
        assertEquals(1, vendorNotifyPersistence.load(id).getRetryCount().intValue());
        vendorNotifyPersistence.updateNotifyDeliveryFailed(id);
        assertEquals(2, vendorNotifyPersistence.load(id).getRetryCount().intValue());
        vendorNotifyPersistence.updateNotifyDeliveryFailed(id);
        assertEquals(3, vendorNotifyPersistence.load(id).getRetryCount().intValue());
    }
}
