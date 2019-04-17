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

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.entity.Vendor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestVendorPersistence {
    @Inject private VendorPersistence vendorPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = Vendor.class)
    public void test_loadAll() throws Exception {
        assertEquals(0, vendorPersistence.loadAll().size());
        vendorPersistence.insert(Vendor.mockInstance());
        vendorPersistence.insert(Vendor.mockInstance());
        vendorPersistence.insert(Vendor.mockInstance());
        vendorPersistence.insert(Vendor.mockInstance().withDisabled(Boolean.TRUE));
        vendorPersistence.insert(Vendor.mockInstance().withDisabled(Boolean.TRUE));
        Map<Long, Vendor> map = vendorPersistence.loadAll();
        assertEquals(5, map.size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = Vendor.class)
    public void test_disable() throws Exception {
        Vendor document = Vendor.mockInstance();
        vendorPersistence.insert(document);
        Long v = document.getId();
        assertFalse(vendorPersistence.loadAll().get(v).isDisabledTrue());
        assertEquals(1, vendorPersistence.disable(v));
        assertTrue(vendorPersistence.loadAll().get(v).isDisabledTrue());
    }
}
