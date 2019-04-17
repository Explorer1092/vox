package com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.SelfStudyHomework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2017/2/16
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSelfStudyHomeworkDao {

    @Inject private SelfStudyHomeworkDao selfStudyHomeworkDao;

    @Test
    public void testInsertSelfStudyHomework() throws Exception {
        Long studentId = 10001L;

        SelfStudyHomework homework = new SelfStudyHomework();
        String month = MonthRange.newInstance(System.currentTimeMillis()).toString();
        String id = new SelfStudyHomework.ID(month, studentId).toString();
        homework.setId(id);
        homework.setSubject(Subject.ENGLISH);
        homework.setStudentId(studentId);
        selfStudyHomeworkDao.insert(homework);

        homework = selfStudyHomeworkDao.load(id);

        assertEquals(Subject.ENGLISH, homework.getSubject());
        selfStudyHomeworkDao.load(id);
    }
}
