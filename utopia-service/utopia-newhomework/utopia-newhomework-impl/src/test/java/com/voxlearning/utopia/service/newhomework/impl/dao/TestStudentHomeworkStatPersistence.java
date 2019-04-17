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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = StudentHomeworkStat.class)
public class TestStudentHomeworkStatPersistence {

    @Inject private StudentHomeworkStatPersistence studentHomeworkStatPersistence;

    @Test
    public void testPersist() throws Exception {
        assertEquals(0, studentHomeworkStatPersistence.queryIdsByClazzId(0L).size());
        assertEquals(0, studentHomeworkStatPersistence.queryIdsByTeacherIdAndClazzId(0L, 0L).size());

        Long id = studentHomeworkStatPersistence.persist(StudentHomeworkStat.mockInstance());

        String ck = StudentHomeworkStat.ck_teacherId_clazzId_studentId(0L, 0L, 0L);
        StudentHomeworkStat c = studentHomeworkStatPersistence.getCache().load(ck);
        assertEquals(id, c.getId());
        assertEquals(0, c.getFinishHomeworkCount().longValue());
        assertEquals(0, c.getFinishQuizCount().longValue());
        assertEquals(0, c.getFinishSubjectiveCount().longValue());
        assertEquals(0, c.getFinishOralCount().longValue());
        assertEquals(0, c.getFinishO2OOfflineCount().longValue());
        assertEquals(0, c.getFinishO2OOnlineCount().longValue());
        assertEquals(0, c.getFinishVhCount().longValue());

        assertEquals(1, studentHomeworkStatPersistence.queryIdsByClazzId(0L).size());
        assertEquals(1, studentHomeworkStatPersistence.queryIdsByTeacherIdAndClazzId(0L, 0L).size());
    }

    @Test
    public void testIncFinishHomeworkCount() throws Exception {
        Long id = studentHomeworkStatPersistence.persist(StudentHomeworkStat.mockInstance());
        assertEquals(0, studentHomeworkStatPersistence.loadFromDatabase(id).getFinishHomeworkCount().longValue());
        assertTrue(studentHomeworkStatPersistence.incFinishHomeworkCount(0L, 0L, 0L));
        assertEquals(1, studentHomeworkStatPersistence.loadFromDatabase(id).getFinishHomeworkCount().longValue());
    }

}
