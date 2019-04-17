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

package com.voxlearning.utopia.service.mentor.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.MentorLevel;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author RuiBao
 * @version 0.1
 * @since 5/8/2015
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = MentorHistory.class)
public class TestMentorHistoryPersistence {
    @Inject private MentorHistoryPersistence mentorHistoryPersistence;

    @Test
    public void testUpdateSuccess() throws Exception {
        MentorHistory history = new MentorHistory();
        history.setMentorId(0L);
        history.setMenteeId(0L);
        history.setSuccess(false);
        mentorHistoryPersistence.insert(history);
        Long id = history.getId();
        mentorHistoryPersistence.updateSuccess(id);
        history = mentorHistoryPersistence.load(id);
        assertTrue(history.getSuccess());
    }

    @Test
    public void testUpdateLevel() throws Exception {
        MentorHistory history = new MentorHistory();
        history.setMentorId(0L);
        history.setMenteeId(0L);
        history.setMentorLevel(MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_ONE.name());
        mentorHistoryPersistence.insert(history);
        Long id = history.getId();
        mentorHistoryPersistence.updateLevel(id, MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_TWO);
        history = mentorHistoryPersistence.load(id);
        assertEquals(MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_TWO.name(), history.getMentorLevel());
    }
}
