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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.homework.api.constant.CheatingTeacherStatus;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingTeacher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestPossibleCheatingTeacherDao {
    @Inject private PossibleCheatingTeacherDao possibleCheatingTeacherDao;

    @Test
    public void testLoadByTeacherId() throws Exception {
        long teacherId = 10001;
        PossibleCheatingTeacher inst = new PossibleCheatingTeacher();
        inst.setTeacherId(teacherId);
        possibleCheatingTeacherDao.insert(inst);
        assertNotNull(possibleCheatingTeacherDao.loadByTeacherId(teacherId));
    }

    @Test
    public void testDisabledById() throws Exception {
        long teacherId = 10001;
        PossibleCheatingTeacher inst = new PossibleCheatingTeacher();
        inst.setTeacherId(teacherId);
        String id = possibleCheatingTeacherDao.insert(inst);
        assertNotNull(possibleCheatingTeacherDao.loadByTeacherId(teacherId));
        possibleCheatingTeacherDao.disabledById(id);
        assertNull(possibleCheatingTeacherDao.loadByTeacherId(teacherId));
    }

    @Test
    public void testLoadByDateRangeAndStatus() throws Exception {
        PossibleCheatingTeacher inst = new PossibleCheatingTeacher();
        inst.setTeacherId(10001L);
        inst.setStatus(CheatingTeacherStatus.WHITE);
        possibleCheatingTeacherDao.insert(inst);
        DateRange range = new DateRange(System.currentTimeMillis() - 10000, System.currentTimeMillis() + 10000);
        assertEquals(1, possibleCheatingTeacherDao.loadByDateRangeAndStatus(range, CheatingTeacherStatus.WHITE).size());
    }
}
