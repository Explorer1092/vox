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
import com.voxlearning.utopia.service.afenti.api.constant.AfentiGuide;
import com.voxlearning.utopia.service.afenti.api.entity.UserAfentiGuide;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserAfentiGuidePersistence {

    @Inject private UserAfentiGuidePersistence userAfentiGuidePersistence;

    @Test
    public void testCompleteGuide() throws Exception {
        UserAfentiGuide guide = userAfentiGuidePersistence.load(1L);
        assertFalse(guide.isCompleted(AfentiGuide.AFENTI_INDEX.name()));
        assertFalse(guide.isCompleted(AfentiGuide.FACTOR_FACTORY.name()));

        userAfentiGuidePersistence.completeGuide(1L, AfentiGuide.AFENTI_INDEX);
        userAfentiGuidePersistence.completeGuide(1L, AfentiGuide.FACTOR_FACTORY);

        guide = userAfentiGuidePersistence.load(1L);
        assertTrue(guide.isCompleted(AfentiGuide.AFENTI_INDEX.name()));
        assertTrue(guide.isCompleted(AfentiGuide.FACTOR_FACTORY.name()));
    }
}
