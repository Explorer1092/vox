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

package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.dao.mysql.support.TruncateCommonVersionTable;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsResgRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateCommonVersionTable
@TruncateDatabaseTable(databaseEntities = VendorAppsResgRef.class)
public class TestVendorAppsResgRefPersistence {
    @Inject private VendorAppsResgRefPersistence vendorAppsResgRefPersistence;

    @Test
    public void test_loadAll() throws Exception {
        assertEquals(0, vendorAppsResgRefPersistence.loadAll().size());
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance());
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance());
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance());
        Map<Long, VendorAppsResgRef> map = vendorAppsResgRefPersistence.loadAll();
        assertEquals(3, map.size());
    }

    @Test
    public void test_deleteByAppId() throws Exception {
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance().withAppId(1L));
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance().withAppId(1L));
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance().withAppId(1L));
        assertEquals(3, vendorAppsResgRefPersistence.loadAll().size());
        assertEquals(3, vendorAppsResgRefPersistence.deleteByAppId(1L));
        assertEquals(0, vendorAppsResgRefPersistence.loadAll().size());
    }

    @Test
    public void test_deleteByResgId() throws Exception {
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance().withResgId(1L));
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance().withResgId(1L));
        vendorAppsResgRefPersistence.insert(VendorAppsResgRef.mockInstance().withResgId(1L));
        assertEquals(3, vendorAppsResgRefPersistence.loadAll().size());
        assertEquals(3, vendorAppsResgRefPersistence.deleteByResgId(1L));
        assertEquals(0, vendorAppsResgRefPersistence.loadAll().size());
    }
}
