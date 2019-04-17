/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.api.constant.ActivationType;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestTeacherActivateTeacherHistoryDao {
    @Autowired private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;

    @Test
    public void testUpdateActivationType() throws Exception {
        TeacherActivateTeacherHistory history = new TeacherActivateTeacherHistory();
        history.setActivationType(ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE);
        String id = teacherActivateTeacherHistoryDao.insert(history);
        assertEquals(ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE, teacherActivateTeacherHistoryDao.load(id).getActivationType());
        teacherActivateTeacherHistoryDao.updateActivationType(id, ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO);
        assertEquals(ActivationType.SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO, teacherActivateTeacherHistoryDao.load(id).getActivationType());
    }

    @Test
    public void testUpdateOver() throws Exception {
        TeacherActivateTeacherHistory history = new TeacherActivateTeacherHistory();
        history.setOver(false);
        String id = teacherActivateTeacherHistoryDao.insert(history);
        assertEquals(false, teacherActivateTeacherHistoryDao.load(id).getOver());
        teacherActivateTeacherHistoryDao.updateOver(id);
        assertEquals(true, teacherActivateTeacherHistoryDao.load(id).getOver());
    }
}
