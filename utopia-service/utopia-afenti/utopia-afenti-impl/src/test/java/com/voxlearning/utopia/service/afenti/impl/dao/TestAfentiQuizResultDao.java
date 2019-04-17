package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.RebuildDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author Ruib
 * @since 2016/10/13
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiQuizResultDao {
    @Inject AfentiQuizResultDao afentiQuizResultDao;

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_QUIZ_RESULT_{}", arguments = {"7"})
    public void testQueryByUserIdAndNewBookId() throws Exception {
        long userId = 30007L;
        String newBookId = "BK_103495837457";

        AfentiQuizResult result = new AfentiQuizResult();
        result.setUserId(userId);
        result.setNewBookId(newBookId);
        result.setNewUnitId("BKC_103111111111");
        result.setKnowledgePoint("BKC_103090909094");
        result.setExamId("Q_235546546123_1");
        result.setRightNum(0);
        result.setErrorNum(0);
        result.setSubject(Subject.ENGLISH);
        afentiQuizResultDao.insert(result);
        assertEquals(1, afentiQuizResultDao.queryByUserIdAndNewBookId(userId, newBookId).size());

        result = new AfentiQuizResult();
        result.setUserId(userId);
        result.setNewBookId(newBookId);
        result.setNewUnitId("BKC_103111111111");
        result.setKnowledgePoint("BKC_103090904678");
        result.setExamId("Q_235546546137_1");
        result.setRightNum(0);
        result.setErrorNum(0);
        result.setSubject(Subject.ENGLISH);
        afentiQuizResultDao.insert(result);
        assertEquals(2, afentiQuizResultDao.queryByUserIdAndNewBookId(userId, newBookId).size());
    }

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_QUIZ_RESULT_{}", arguments = {"1"})
    public void testUpdateRightAndErrorNums() throws Exception {
        long userId = 30001L;
        String newBookId = "BK_103495837457";

        AfentiQuizResult result = new AfentiQuizResult();
        result.setUserId(userId);
        result.setNewBookId(newBookId);
        result.setNewUnitId("BKC_103111111111");
        result.setKnowledgePoint("BKC_103090909094");
        result.setExamId("Q_235546546123_1");
        result.setRightNum(0);
        result.setErrorNum(0);
        result.setSubject(Subject.ENGLISH);
        afentiQuizResultDao.insert(result);
        assertEquals(1, afentiQuizResultDao.queryByUserIdAndNewBookId(userId, newBookId).size());

        result.setRightNum(1);
        result.setErrorNum(2);
        afentiQuizResultDao.updateRightAndErrorNums(result);

        result = afentiQuizResultDao.queryByUserIdAndNewBookId(userId, newBookId).stream().findFirst().orElse(null);
        assertEquals(1, result.getRightNum().intValue());
        assertEquals(2, result.getErrorNum().intValue());
    }
}
