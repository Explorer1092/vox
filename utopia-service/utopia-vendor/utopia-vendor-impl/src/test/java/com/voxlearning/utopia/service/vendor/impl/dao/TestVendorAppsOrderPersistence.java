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
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestVendorAppsOrderPersistence {
    @Inject private VendorAppsOrderPersistence vendorAppsOrderPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = VendorAppsOrder.class)
    public void testFind() throws Exception {
        vendorAppsOrderPersistence.insert(VendorAppsOrder.newInstance(0L, "A", 0L, "S", 1L, 0L, "", ""));
        assertNotNull(vendorAppsOrderPersistence.find("A", "S", 1L));
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = VendorAppsOrder.class)
    public void testFinishPaymnet() throws Exception {
        VendorAppsOrder inst = VendorAppsOrder.newInstance(0L, "", 0L, "", 0L, 0L, "", "");
        vendorAppsOrderPersistence.insert(inst);
        String id = inst.getId();
        assertFalse(vendorAppsOrderPersistence.load(id).isPaid());
        assertEquals(1, vendorAppsOrderPersistence.finishPaymnet(id));
        assertTrue(vendorAppsOrderPersistence.load(id).isPaid());
    }
}
