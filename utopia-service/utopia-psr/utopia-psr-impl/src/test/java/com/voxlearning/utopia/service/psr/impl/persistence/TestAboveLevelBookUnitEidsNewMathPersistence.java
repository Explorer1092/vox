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

package com.voxlearning.utopia.service.psr.impl.persistence;

import com.voxlearning.alps.annotation.cache.IgnoreFlushCache;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.psr.entity.AboveLevelBookUnitEidsNewMath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@DropMongoDatabase
public class TestAboveLevelBookUnitEidsNewMathPersistence {

    @Inject private AboveLevelBookUnitEidsNewMathPersistence aboveLevelBookUnitEidsNewMathPersistence;

    @Test
    public void testAboveLevelBookUnitEidsNewMathPersistence() throws Exception {
        AboveLevelBookUnitEidsNewMath inst = new AboveLevelBookUnitEidsNewMath();
        inst.setBookid("B");
        aboveLevelBookUnitEidsNewMathPersistence.insert(inst);
        inst = aboveLevelBookUnitEidsNewMathPersistence.findByBookId("B");
        assertNotNull(inst);
    }
}
