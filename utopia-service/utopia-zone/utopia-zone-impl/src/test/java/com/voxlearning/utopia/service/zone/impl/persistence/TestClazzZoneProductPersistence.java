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
import com.voxlearning.alps.dao.mysql.support.TruncateCommonVersionTable;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.Currency;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@TruncateCommonVersionTable
@TruncateDatabaseTable(databaseEntities = ClazzZoneProduct.class)
public class TestClazzZoneProductPersistence {
    @Inject private ClazzZoneProductPersistence clazzZoneProductPersistence;

    @Test
    public void testClazzZoneProductDao() throws Exception {
        ClazzZoneProduct document = new ClazzZoneProduct();
        document.setName("");
        document.setPrice(0);
        document.setCurrency(Currency.CRYSTAL);
        document.setSpecies("");
        document.setSubspecies("");
        clazzZoneProductPersistence.insert(document);
        document = clazzZoneProductPersistence.load(document.getId());
        assertNotNull(document);
    }
}
