package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.RebuildDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiQuizStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author Ruib
 * @since 2016/10/12
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiQuizStatDao {
    @Inject private AfentiQuizStatDao afentiQuizStatDao;

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_QUIZ_STAT_{}", arguments = {"7"})
    public void testQueryByUserIdAndNewBookId() throws Exception {
        long userId = 30007L;
        String newBookId = "BK_103495837457";

        AfentiQuizStat stat = new AfentiQuizStat();
        stat.setUserId(userId);
        stat.setNewBookId(newBookId);
        stat.setNewUnitId("BKC_103111111111");
        stat.setScore(0);
        stat.setSilver(0);
        stat.setSubject(Subject.ENGLISH);
        afentiQuizStatDao.insert(stat);
        assertEquals(1, afentiQuizStatDao.queryByUserId(userId).size());

        stat = new AfentiQuizStat();
        stat.setUserId(userId);
        stat.setNewBookId(newBookId);
        stat.setNewUnitId("BKC_103222222222");
        stat.setScore(0);
        stat.setSilver(0);
        stat.setSubject(Subject.ENGLISH);
        afentiQuizStatDao.insert(stat);
        assertEquals(2, afentiQuizStatDao.queryByUserId(userId).size());
    }

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_QUIZ_STAT_{}", arguments = {"1"})
    public void testUpdateScoreAndSilver() throws Exception {
        long userId = 30001L;
        String newBookId = "BK_103495837457";

        AfentiQuizStat stat = new AfentiQuizStat();
        stat.setUserId(userId);
        stat.setNewBookId(newBookId);
        stat.setNewUnitId("BKC_103111111111");
        stat.setScore(0);
        stat.setSilver(0);
        stat.setSubject(Subject.ENGLISH);
        afentiQuizStatDao.insert(stat);
        assertEquals(1, afentiQuizStatDao.queryByUserId(userId).size());

        stat.setSilver(20);
        stat.setScore(10);
        afentiQuizStatDao.updateScoreAndSilver(stat);

        stat = afentiQuizStatDao.queryByUserId(userId).stream().findFirst().orElse(null);
        assertEquals(10, stat.getScore().intValue());
        assertEquals(20, stat.getSilver().intValue());
    }
}
