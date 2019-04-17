package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.activity.TermBeginStudentAppRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = TermBeginStudentAppRecord.class)
public class TestTermBeginStudentAppRecordPersistence {

    @Inject private TermBeginStudentAppRecordPersistence termBeginStudentAppRecordPersistence;

    @Test
    public void testFindByTeacherId() throws Exception {
        for (int i = 0; i < 3; i++) {
            TermBeginStudentAppRecord record = new TermBeginStudentAppRecord();
            record.setTeacherId(10000L);
            termBeginStudentAppRecordPersistence.insert(record);
            assertEquals(i + 1, termBeginStudentAppRecordPersistence.findByTeacherId(10000L).size());
        }
    }
}
