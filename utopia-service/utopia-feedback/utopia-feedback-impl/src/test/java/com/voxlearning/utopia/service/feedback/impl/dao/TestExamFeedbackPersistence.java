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
import com.voxlearning.utopia.service.feedback.api.entities.ExamFeedback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestExamFeedbackPersistence {
    @Autowired private ExamFeedbackPersistence examFeedbackPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ExamFeedback.class)
    public void testExamFeedbackPersistence() throws Exception {
        ExamFeedback feedback = new ExamFeedback();
        feedback.setUserId(1L);
        feedback.setQuestionId("QUESTION");
        feedback.setContent("CONTENT");
        feedback.setType("TYPE");
        examFeedbackPersistence.insert(feedback);
        Long id = feedback.getId();
        feedback = examFeedbackPersistence.load(id);
        assertEquals(1, feedback.getUserId().longValue());
        assertEquals("QUESTION", feedback.getQuestionId());
        assertEquals("CONTENT", feedback.getContent());
        assertEquals("TYPE", feedback.getType());
    }
}
