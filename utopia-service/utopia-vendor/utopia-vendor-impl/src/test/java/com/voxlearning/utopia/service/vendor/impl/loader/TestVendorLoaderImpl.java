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

package com.voxlearning.utopia.service.vendor.impl.loader;

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.annotation.TruncateVendors;
import com.voxlearning.utopia.service.vendor.api.entity.Vendor;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.api.entity.VendorResg;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorAppsUserRefDao;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorPersistence;
import com.voxlearning.utopia.service.vendor.impl.dao.VendorResgPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestVendorLoaderImpl {

    @Inject private VendorAppsUserRefDao vendorAppsUserRefDao;
    @Inject private VendorPersistence vendorPersistence;
    @Inject private VendorResgPersistence vendorResgPersistence;
    @Inject private VendorLoaderClient vendorLoaderClient;
    @Inject private VendorLoaderImpl vendorLoader;

    @Test
    @TruncateVendors
    public void test_loadVendorsIncludeDisabled() throws Exception {
        assertEquals(0, vendorLoaderClient.loadVendorsIncludeDisabled().size());
        assertEquals(0, vendorLoader.loadVendorsIncludeDisabled().size());
        vendorPersistence.insert(Vendor.mockInstance());
        vendorPersistence.insert(Vendor.mockInstance());
        vendorPersistence.insert(Vendor.mockInstance());
        vendorPersistence.insert(Vendor.mockInstance().withDisabled(Boolean.TRUE));
        vendorPersistence.insert(Vendor.mockInstance().withDisabled(Boolean.TRUE));
        Map<Long, Vendor> map = vendorLoaderClient.loadVendorsIncludeDisabled();
        assertEquals(5, map.size());
        map = vendorLoader.loadVendorsIncludeDisabled();
        assertEquals(5, map.size());
    }

    @Test
    @TruncateVendors
    public void test_loadVendorAppUserRefs_byUserId() throws Exception {
        String A = RandomUtils.nextObjectId();
        Collection<Long> userIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        for (Long userId : userIds) {
            VendorAppsUserRef ref = VendorAppsUserRef.mockInstance();
            ref.setAppKey(A);
            ref.setUserId(userId);
            vendorAppsUserRefDao.insert(ref);
        }
        Map<Long, VendorAppsUserRef> map = vendorLoaderClient.loadVendorAppUserRefs(A, userIds);
        assertEquals(userIds.size(), map.size());
        for (Long userId : userIds) {
            assertNotNull(map.get(userId));
            assertNotNull(vendorLoaderClient.loadVendorAppUserRef(A, userId));
        }
        map = vendorLoader.loadVendorAppUserRefs(A, userIds);
        assertEquals(userIds.size(), map.size());
        for (Long userId : userIds) {
            assertNotNull(map.get(userId));
            assertNotNull(vendorLoader.loadVendorAppUserRef(A, userId));
        }
    }

    @Test
    @TruncateVendors
    public void test_loadVendorResgsIncludeDisabled() throws Exception {
        assertEquals(0, vendorResgPersistence.loadAll().size());
        vendorResgPersistence.insert(VendorResg.mockInstance());
        vendorResgPersistence.insert(VendorResg.mockInstance());
        vendorResgPersistence.insert(VendorResg.mockInstance());
        vendorResgPersistence.insert(VendorResg.mockInstance().withDisabled(Boolean.TRUE));
        vendorResgPersistence.insert(VendorResg.mockInstance().withDisabled(Boolean.TRUE));
        Map<Long, VendorResg> map = vendorLoaderClient.loadVendorResgsIncludeDisabled();
        assertEquals(5, map.size());
        map = vendorLoader.loadVendorResgsIncludeDisabled();
        assertEquals(5, map.size());
    }

}
