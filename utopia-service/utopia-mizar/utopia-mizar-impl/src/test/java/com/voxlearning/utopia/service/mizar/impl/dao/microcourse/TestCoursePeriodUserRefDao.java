package com.voxlearning.utopia.service.mizar.impl.dao.microcourse;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.microcourse.CoursePeriodUserRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Yuechen.Wang on 2016/12/21.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestCoursePeriodUserRefDao {

    @Inject private CoursePeriodUserRefDao coursePeriodUserRefDao;

    @Test
    public void updateUserPeriodRef() throws Exception {
        for (int i = 0; i < 100; ++i) {
            CoursePeriodUserRef ref = new CoursePeriodUserRef();
            ref.setPeriodId("P" + i % 2);
            ref.setUserId("" + i);
            coursePeriodUserRefDao.insert(ref);
        }
        assertEquals(50, coursePeriodUserRefDao.findByPeriod("P1").size());

        assertEquals(3, coursePeriodUserRefDao.updateUserPeriodRef("P0", Arrays.asList(0L, 1L, 2L, 8L)));

        Set<Long> p0 = coursePeriodUserRefDao.findByPeriod("P0")
                .stream()
                .filter(ref -> !Boolean.TRUE.equals(ref.getNotified()))
                .map(ref -> SafeConverter.toLong(ref.getUserId()))
                .filter(id -> id > 0)
                .collect(Collectors.toSet());

        assertEquals(47, p0.size());

        assertEquals(5, coursePeriodUserRefDao.updateUserPeriodRef("P0", Arrays.asList(4L, 14L, 22L, 88L,90L)));

        p0 = coursePeriodUserRefDao.findByPeriod("P0")
                .stream()
                .filter(ref -> !Boolean.TRUE.equals(ref.getNotified()))
                .map(ref -> SafeConverter.toLong(ref.getUserId()))
                .filter(id -> id > 0)
                .collect(Collectors.toSet());

        assertEquals(42, p0.size());
    }

}