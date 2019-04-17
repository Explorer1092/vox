package com.voxlearning.utopia.service.action.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.document.UserAttendanceLog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @author xinxin
 * @since 19/8/2016
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserAttendanceLogDao {
    @Inject
    private UserAttendanceLogDao userAttendanceLogDao;

    @Test
    public void testFindByClazzId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String day = formatter.format(LocalDateTime.now());

        UserAttendanceLog log = new UserAttendanceLog();
        log.setId(2001L + "-" + 30008 + "-" + day);
        log.setClazzId(2001L);
        log.setUserId(30008L);
        log.setSignDate(new Date());

        userAttendanceLogDao.insert(log);

        List<UserAttendanceLog> logs = userAttendanceLogDao.findByClazzId(2001L);

        Assert.assertEquals(1, logs.size());
    }
}
