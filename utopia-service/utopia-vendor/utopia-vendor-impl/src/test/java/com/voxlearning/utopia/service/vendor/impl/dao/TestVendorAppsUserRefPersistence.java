/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = VendorAppsUserRef.class)
public class TestVendorAppsUserRefPersistence {
    @Inject private VendorAppsUserRefDao vendorAppsUserRefDao;

    @Test
    public void testUpdateSessionKey() throws Exception {
        VendorAppsUserRef ref = VendorAppsUserRef.mockInstance();
        ref.setAppKey("a");
        ref.setUserId(1L);
        ref.setSessionKey("s1");
        vendorAppsUserRefDao.insert(ref);

        assertEquals(1, vendorAppsUserRefDao.findVendorAppsUserRefList(1L).size());

        long rows = vendorAppsUserRefDao.updateSessionKey("a", 1L, "s2");
        assertEquals(1, rows);

        assertEquals(1, vendorAppsUserRefDao.findVendorAppsUserRefList(1L).size());
    }
}
