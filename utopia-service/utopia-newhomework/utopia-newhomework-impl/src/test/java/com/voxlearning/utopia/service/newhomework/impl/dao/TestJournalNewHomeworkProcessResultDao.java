package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

@DropMongoDatabase
public class TestJournalNewHomeworkProcessResultDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testInsert() throws Exception {
        JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
        result.setBookId("B_1234");

        //journalNewHomeworkProcessResultDao.saveJournalNewHomeworkProcessResults(Collections.singletonList(result));
        journalNewHomeworkProcessResultDao.load(result.getId());

    }
}
