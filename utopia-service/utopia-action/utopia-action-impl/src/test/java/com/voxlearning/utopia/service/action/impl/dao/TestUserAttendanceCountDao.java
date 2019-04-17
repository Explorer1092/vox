package com.voxlearning.utopia.service.action.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.document.UserAttendanceCount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * @author xinxin
 * @since 11/10/2016
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserAttendanceCountDao {
    @Inject
    private UserAttendanceCountDao userAttendanceCountDao;

    @Test
    public void testIncrUserAttendanceCount() {
        UserAttendanceCount userAttendanceCount = userAttendanceCountDao.incrUserAttendanceCount(30008L);

        userAttendanceCountDao.incrUserAttendanceCount(30008L);

        UserAttendanceCount userAttendanceCount1 = userAttendanceCountDao.load(userAttendanceCount.getId());

        Assert.assertEquals(2,userAttendanceCount1.getCount().intValue());
    }

}
