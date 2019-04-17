package com.voxlearning.utopia.service.newhomework.impl.dao.report;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newhomework.api.entity.report.WeekPushTeacher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = WeekPushTeacher.class)
public class TestWeekPushTeacherPersistence {
    @Inject
    private WeekPushTeacherPersistence weekPushTeacherPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = WeekPushTeacher.class)
    public void testLoadWeekPushTeacherByPage() {
        WeekPushTeacher weekPushTeacher = new WeekPushTeacher();
        weekPushTeacher.setTeacherId(12L);
        weekPushTeacher.setSubject_key(1);
        weekPushTeacher.setCreateTime(new Date());
        weekPushTeacherPersistence.insert(weekPushTeacher);
        WeekPushTeacher weekPushTeacher1 = weekPushTeacherPersistence.load(weekPushTeacher.getTeacherId());

        assertNotNull(weekPushTeacherPersistence.load(weekPushTeacher.getTeacherId()));

    }


}
