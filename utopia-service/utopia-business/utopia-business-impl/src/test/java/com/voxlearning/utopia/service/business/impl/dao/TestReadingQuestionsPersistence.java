/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.utopia.entity.content.ReadingQuestions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by tanguohong on 14-7-28.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ReadingQuestions.class)
public class TestReadingQuestionsPersistence {
    @Inject private ReadingQuestionsPersistence readingQuestionsPersistence;

    @Test
    public void testReadingQuestionsPersistence() {
        ReadingQuestions document = new ReadingQuestions();
        document.setReadingId(0L);
        document.setType(0);
        document.setContent("");
        document.setRank(0);
        readingQuestionsPersistence.insert(document);
        assertNotNull(readingQuestionsPersistence.load(document.getId()));
        assertEquals(1, readingQuestionsPersistence.findByReadingId(0L).size());
    }
}
