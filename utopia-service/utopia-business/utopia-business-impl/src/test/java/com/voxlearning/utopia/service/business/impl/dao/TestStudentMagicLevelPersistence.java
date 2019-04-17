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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.activity.StudentMagicLevel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = StudentMagicLevel.class)
public class TestStudentMagicLevelPersistence {

    @Inject private StudentMagicLevelPersistence studentMagicLevelPersistence;

    @Test
    public void testStudentMagicLevelPersistence() throws Exception {
        StudentMagicLevel document = new StudentMagicLevel();
        document.setMagicianId(1L);
        studentMagicLevelPersistence.insert(document);

        studentMagicLevelPersistence.updateLevel(1L, 10);
        studentMagicLevelPersistence.updateLevelValue(1L, 100);

        document = studentMagicLevelPersistence.findByMagicianId(1L);
        assertEquals(10, document.getLevel().intValue());
        assertEquals(100, document.getLevelValue().intValue());
    }
}
