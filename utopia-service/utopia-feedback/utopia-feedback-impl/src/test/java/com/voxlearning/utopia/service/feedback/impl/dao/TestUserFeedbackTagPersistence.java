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

package com.voxlearning.utopia.service.feedback.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.feedback.api.entities.UserFeedbackTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = UserFeedbackTag.class)
public class TestUserFeedbackTagPersistence {
    @Inject private UserFeedbackTagPersistence userFeedbackTagPersistence;

    @Test
    public void testFindAllTags() throws Exception {
        userFeedbackTagPersistence.insert(UserFeedbackTag.newInstance("A"));
        assertEquals(1, userFeedbackTagPersistence.findAllTags().size());
        userFeedbackTagPersistence.insert(UserFeedbackTag.newInstance("B"));
        assertEquals(2, userFeedbackTagPersistence.findAllTags().size());
        userFeedbackTagPersistence.insert(UserFeedbackTag.newInstance("C"));
        assertEquals(3, userFeedbackTagPersistence.findAllTags().size());
    }
}
