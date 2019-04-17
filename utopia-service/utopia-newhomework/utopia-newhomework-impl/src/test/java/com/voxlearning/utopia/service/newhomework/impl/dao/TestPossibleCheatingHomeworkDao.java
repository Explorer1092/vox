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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.PossibleCheatingHomework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 6/18/2015
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestPossibleCheatingHomeworkDao {
    @Inject private PossibleCheatingHomeworkDao possibleCheatingHomeworkDao;

    @Test
    public void testGetByTeacherIdAndHomeworkId() throws Exception {
        PossibleCheatingHomework pch = new PossibleCheatingHomework();
        pch.setHomeworkId("1");
        pch.setTeacherId(10002L);
        pch.setHomeworkType(HomeworkType.CHINESE);
        String id = possibleCheatingHomeworkDao.insert(pch);
        assertNotNull(possibleCheatingHomeworkDao.getByTeacherIdAndHomeworkId(10002L, "1", HomeworkType.CHINESE));
        possibleCheatingHomeworkDao.delete(id);
        assertNull(possibleCheatingHomeworkDao.getByTeacherIdAndHomeworkId(10002L, "1", HomeworkType.CHINESE));

        PossibleCheatingHomework pch1 = new PossibleCheatingHomework();
        pch1.setHomeworkId("1");
        pch1.setTeacherId(10002L);
        pch1.setHomeworkType(HomeworkType.CHINESE);
        pch1.setRecordOnly(false);
        possibleCheatingHomeworkDao.insert(pch1);
        assertNotNull(possibleCheatingHomeworkDao.getByTeacherIdAndHomeworkId(10002L, "1", HomeworkType.CHINESE));
    }

    @Test
    public void testGetByDateRange() throws Exception {
        DateRange range = new DateRange(DateUtils.stringToDate("2015-06-18 00:00:00"), DateUtils.stringToDate("2015-06-18 23:59:59"));
        PossibleCheatingHomework pch1 = new PossibleCheatingHomework();
        pch1.setCreateDatetime(DateUtils.stringToDate("2015-06-18 10:00:00"));
        possibleCheatingHomeworkDao.insert(pch1);
        PossibleCheatingHomework pch2 = new PossibleCheatingHomework();
        pch2.setCreateDatetime(DateUtils.stringToDate("2015-06-18 12:00:00"));
        possibleCheatingHomeworkDao.insert(pch2);

        assertEquals(2, possibleCheatingHomeworkDao.getByDateRange(range).size());
    }
}
