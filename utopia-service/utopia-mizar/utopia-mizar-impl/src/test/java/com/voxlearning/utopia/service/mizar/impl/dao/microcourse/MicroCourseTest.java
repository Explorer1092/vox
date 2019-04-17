package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Created by Yuechen.Wang on 2016/12/9.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class MicroCourseTest {
    @Inject private MicroCourseDao microCourseDao;
    @Inject private MicroCoursePeriodDao microCoursePeriodDao;
    @Inject private MicroCourseUserRefDao microCourseUserRefDao;
    @Inject private MicroCoursePeriodRefDao microCoursePeriodRefDao;


    @Test
    public void testMicroCourse() {

    }

}