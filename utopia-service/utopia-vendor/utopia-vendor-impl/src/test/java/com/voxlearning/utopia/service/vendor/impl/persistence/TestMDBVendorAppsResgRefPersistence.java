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

package com.voxlearning.utopia.service.vendor.impl.persistence;

import com.voxlearning.alps.annotation.cache.IgnoreFlushCache;
import com.voxlearning.alps.spi.test.TruncateMDB;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.mdb.MDBVendorAppsResgRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@TruncateMDB(entities = MDBVendorAppsResgRef.class)
public class TestMDBVendorAppsResgRefPersistence {

    @Inject private MDBVendorAppsResgRefPersistence mdbVendorAppsResgRefPersistence;

    @Test
    public void testFindByAppKey() throws Exception {
        for (long i = 1; i <= 5; i++) {
            MDBVendorAppsResgRef ref = new MDBVendorAppsResgRef();
            ref.setId(i);
            ref.setAppKey("A");
            mdbVendorAppsResgRefPersistence.insert(ref);
        }
        List<MDBVendorAppsResgRef> list = mdbVendorAppsResgRefPersistence.findByAppKey("A");
        assertEquals(5, list.size());
    }
}
