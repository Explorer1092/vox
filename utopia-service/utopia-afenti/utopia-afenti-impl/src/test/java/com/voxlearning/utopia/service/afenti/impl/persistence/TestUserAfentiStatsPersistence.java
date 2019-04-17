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

package com.voxlearning.utopia.service.afenti.impl.persistence;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiStats;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserAfentiStatsPersistence {
    @Inject private UserAfentiStatsPersistence userAfentiStatsPersistence;

    @Test
    public void testUpdateStats() throws Exception {
        UserAfentiStats inst = userAfentiStatsPersistence.load(1L);
        assertNull(inst);

        userAfentiStatsPersistence.updateStats(1L, "A", "a");
        inst = userAfentiStatsPersistence.load(1L);
        assertEquals("a", inst.getStats().get("A"));

        userAfentiStatsPersistence.updateStats(1L, "B", "b");
        inst = userAfentiStatsPersistence.load(1L);
        assertEquals("b", inst.getStats().get("B"));

        userAfentiStatsPersistence.updateStats(1L, "B", null);
        inst = userAfentiStatsPersistence.load(1L);
        assertNull(inst.getStats().get("B"));
    }
}
