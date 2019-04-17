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
import com.voxlearning.utopia.service.business.api.entity.RSTeacherAuthStudentCountDaily;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author changyuan.liu
 * @since 2015/5/13
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSTeacherAuthStudentCountDailyDao {
    @Autowired private RSTeacherAuthStudentCountDailyDao rsTeacherAuthStudentCountDailyDao;

    @Test
    public void testFindByTeacherId() throws Exception {
        long teacherId = 123L;

        RSTeacherAuthStudentCountDaily data1 = new RSTeacherAuthStudentCountDaily();
        data1.setDay(20150510);
        data1.setTeacherId(teacherId);
        data1.setStudentCount(10);
        data1.setDuplicateStudentCount(1);

        RSTeacherAuthStudentCountDaily data2 = new RSTeacherAuthStudentCountDaily();
        data2.setDay(20150511);
        data2.setTeacherId(teacherId);
        data2.setStudentCount(15);
        data2.setDuplicateStudentCount(2);

        RSTeacherAuthStudentCountDaily data3 = new RSTeacherAuthStudentCountDaily();
        data3.setDay(20150512);
        data3.setTeacherId(teacherId);
        data3.setStudentCount(20);
        data3.setDuplicateStudentCount(3);

        rsTeacherAuthStudentCountDailyDao.insert(data1);
        rsTeacherAuthStudentCountDailyDao.insert(data2);
        rsTeacherAuthStudentCountDailyDao.insert(data3);

        List<RSTeacherAuthStudentCountDaily> ret = rsTeacherAuthStudentCountDailyDao.findByTeacherIds(Collections.singleton(teacherId), null, null);
        assertEquals(3, ret.size());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2015, 4, 9);
        Date startDate = calendar.getTime();
        calendar.set(2015, 4, 11);
        Date endDate = calendar.getTime();
        ret = rsTeacherAuthStudentCountDailyDao.findByTeacherIds(Collections.singleton(teacherId), startDate, endDate);
        assertEquals(2, ret.size());
    }
}
