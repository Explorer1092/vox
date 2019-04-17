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
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * @author changyuan.liu
 * @since 2015/3/4
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestClazzJournalPersistence {

    @Inject private ClazzJournalPersistence clazzJournalPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    public void testQueryByClazzId() {
        long clazzId = 1;
        for (int i = 0; i < 5; i++) {
            ClazzJournal journal = ClazzJournal.mockInstance();
            journal.setClazzId(clazzId);
            clazzJournalPersistence.persist(journal);
            assertEquals(i + 1, clazzJournalPersistence.queryByClazzId(clazzId).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    public void testQueryByUserId() throws Exception {
        long userId = 1;
        for (int i = 0; i < 5; i++) {
            ClazzJournal journal = ClazzJournal.mockInstance();
            journal.setRelevantUserId(userId);
            clazzJournalPersistence.persist(journal);
            assertEquals(i + 1, clazzJournalPersistence.queryByUserId(userId).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    public void testQueryByJournalType() throws Exception {
        ClazzJournalType journalType = ClazzJournalType.TEACHER_ASSIGN_HOMEWORK;
        for (int i = 0; i < 5; i++) {
            ClazzJournal journal = ClazzJournal.mockInstance();
            journal.setJournalType(journalType);
            clazzJournalPersistence.persist(journal);
            assertEquals(i + 1, clazzJournalPersistence.queryByJournalType(journalType.getId()).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            persistence = ClazzJournalPersistence.class
    )
    public void testIncreaseLikeCount() throws Exception {
        Long id = MDP.findOne(ClazzJournal.class).getId();
        assertEquals(0, clazzJournalPersistence.load(id).getLikeCount().intValue());
        clazzJournalPersistence.increaseLikeCount(id);
        assertEquals(1, clazzJournalPersistence.load(id).getLikeCount().intValue());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            persistence = ClazzJournalPersistence.class
    )
    public void testUpdateJournalJson() throws Exception {
        Long id = MDP.findOne(ClazzJournal.class).getId();
        assertNull(clazzJournalPersistence.load(id).getJournalJson());
        clazzJournalPersistence.updateJournalJson(id, "{}");
        assertEquals("{}", clazzJournalPersistence.load(id).getJournalJson());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            persistence = ClazzJournalPersistence.class
    )
    public void testDeleteJournal() throws Exception {
        Long id = MDP.findOne(ClazzJournal.class).getId();
        assertNotNull(clazzJournalPersistence.load(id));
        assertEquals(1, clazzJournalPersistence.queryByClazzId(1L).size());
        assertEquals(1, clazzJournalPersistence.queryByUserId(1L).size());
        assertEquals(1, clazzJournalPersistence.queryByJournalType(ClazzJournalType.TEACHER_ASSIGN_HOMEWORK.getId()).size());

        assertEquals(1, clazzJournalPersistence.deleteJournal(id, 1L));

        assertNull(clazzJournalPersistence.load(id));
        assertEquals(0, clazzJournalPersistence.queryByClazzId(1L).size());
        assertEquals(0, clazzJournalPersistence.queryByUserId(1L).size());
        assertEquals(0, clazzJournalPersistence.queryByJournalType(ClazzJournalType.TEACHER_ASSIGN_HOMEWORK.getId()).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = ClazzJournal.class)
    @MockBinder(
            type = ClazzJournal.class,
            jsons = "{'id':$nextLong,'clazzId':1,'relevantUserId':1,'relevantUserType':'TEACHER','journalType':'TEACHER_ASSIGN_HOMEWORK'}",
            persistence = ClazzJournalPersistence.class
    )
    public void testDelJournal() throws Exception {
        Long id = MDP.findOne(ClazzJournal.class).getId();
        assertNotNull(clazzJournalPersistence.load(id));
        assertEquals(1, clazzJournalPersistence.queryByClazzId(1L).size());
        assertEquals(1, clazzJournalPersistence.queryByUserId(1L).size());
        assertEquals(1, clazzJournalPersistence.queryByJournalType(ClazzJournalType.TEACHER_ASSIGN_HOMEWORK.getId()).size());

        assertEquals(1, clazzJournalPersistence.delJournal(id));

        assertNull(clazzJournalPersistence.load(id));
        assertEquals(0, clazzJournalPersistence.queryByClazzId(1L).size());
        assertEquals(0, clazzJournalPersistence.queryByUserId(1L).size());
        assertEquals(0, clazzJournalPersistence.queryByJournalType(ClazzJournalType.TEACHER_ASSIGN_HOMEWORK.getId()).size());
    }
}
