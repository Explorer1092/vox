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

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.alps.test.util.UnitTestUtils;
import com.voxlearning.utopia.service.zone.api.entity.ClazzZoneBag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ClazzZoneBag.class)
public class TestClazzZoneBagPersistence {

    @Inject private ClazzZoneBagPersistence clazzZoneBagPersistence;

    @Test
    public void testDelete() throws Exception {
        long userId = UnitTestUtils.nextId();
        ClazzZoneBag document = new ClazzZoneBag();
        document.setUserId(userId);
        document.setProductId(0L);
        document.setExpireDate(new Date());
        clazzZoneBagPersistence.insert(document);
        Long id = document.getId();
        assertEquals(1, clazzZoneBagPersistence.findByUserId(userId).size());
        assertTrue(clazzZoneBagPersistence.delete(id, userId));
        assertEquals(0, clazzZoneBagPersistence.findByUserId(userId).size());
    }
}
