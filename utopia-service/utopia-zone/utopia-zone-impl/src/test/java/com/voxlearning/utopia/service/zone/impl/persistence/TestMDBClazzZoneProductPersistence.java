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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.annotation.cache.IgnoreFlushCache;
import com.voxlearning.alps.spi.test.TruncateMDB;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.mdb.MDBClazzZoneProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@TruncateMDB(entities = MDBClazzZoneProduct.class)
public class TestMDBClazzZoneProductPersistence {

    @Inject private MDBClazzZoneProductPersistence mdbClazzZoneProductPersistence;

    @Test
    public void testMDBClazzZoneProductPersistence() throws Exception {
        MDBClazzZoneProduct product = new MDBClazzZoneProduct();
        product.setId(1L);
        product.setSpecies("S");
        mdbClazzZoneProductPersistence.insert(product);
        List<MDBClazzZoneProduct> list = mdbClazzZoneProductPersistence.findBySpecies("S");
        assertEquals(1, list.size());
    }
}
