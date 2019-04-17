package com.voxlearning.utopia.service.action.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.document.ClazzAttendanceCount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author xinxin
 * @since 24/8/2016
 */

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestClazzAttendanceCountDao {
    @Inject
    private ClazzAttendanceCountDao clazzAttendanceCountDao;

    @Test
    public void testIncrAttendanceCount() {
        clazzAttendanceCountDao.incrAttendanceCount(20001L, 2003L, 10);

        ClazzAttendanceCount count1 = clazzAttendanceCountDao.incrAttendanceCount(20001L,2003L,10);

        Assert.assertEquals(10,count1.getTotalCount().intValue());
        Assert.assertEquals(2,count1.getCount().intValue());

    }
}
