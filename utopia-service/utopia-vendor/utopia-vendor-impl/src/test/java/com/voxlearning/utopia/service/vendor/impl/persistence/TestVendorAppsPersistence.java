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
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateCommonVersionTable
@TruncateDatabaseTable(databaseEntities = VendorApps.class)
public class TestVendorAppsPersistence {

    @Inject private VendorAppsPersistence vendorAppsPersistence;

    @Test
    public void testDisable() throws Exception {
        VendorApps document = new VendorApps();
        document.setVendorId(0L);
        document.setCname("");
        document.setShortName("");
        document.setAppUrl("");
        document.setAppIcon("");
        document.setAppKey("");
        document.setSecretKey("");
        document.setDayMaxAccess(0L);
        document.setDayMaxAddPK(0);
        document.setDayMaxAddIntegral(0);
        document.setStatus("");
        vendorAppsPersistence.insert(document);
        Long id = document.getId();
        assertEquals(1, vendorAppsPersistence.disable(id));
        document = vendorAppsPersistence.load(id);
        assertTrue(document.getDisabled());
    }
}
