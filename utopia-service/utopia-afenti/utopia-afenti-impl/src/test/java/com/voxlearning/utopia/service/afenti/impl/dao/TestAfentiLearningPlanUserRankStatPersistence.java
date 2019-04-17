package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserRankStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiLearningPlanUserRankStatPersistence {
    @Inject private AfentiLearningPlanUserRankStatPersistence p;

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserRankStat.class)
    public void testPersistence() throws Exception {
        assertEquals(0, p.queryByUserIdAndNewBookId(30013L, "BK-103000000").size());

        AfentiLearningPlanUserRankStat stat = newInstance();
        stat.setUserId(30013L);
        stat.setNewBookId("BK-103000000");
        p.persist(stat);

        assertEquals(1, p.queryByUserIdAndNewBookId(30013L, "BK-103000000").size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserRankStat.class)
    public void testQueryByUserIdAndNewBookId() throws Exception {
        assertEquals(0, p.queryByUserIdAndNewBookId(30013L, "BK-103000000").size());

        AfentiLearningPlanUserRankStat stat = newInstance();
        stat.setUserId(30013L);
        stat.setNewBookId("BK-103000000");
        p.persist(stat);

        assertEquals(1, p.queryByUserIdAndNewBookId(30013L, "BK-103000000").size());

        stat = newInstance();
        stat.setUserId(30013L);
        stat.setNewBookId("BK-103000000");
        p.persist(stat);

        assertEquals(2, p.queryByUserIdAndNewBookId(30013L, "BK-103000000").size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserRankStat.class)
    public void testQueryTotalStar() throws Exception {
        assertEquals(0, p.queryTotalStar(30013L, Subject.ENGLISH));

        AfentiLearningPlanUserRankStat stat = newInstance();
        stat.setUserId(30013L);
        stat.setNewBookId("BK-103000000");
        stat.setStar(2);
        p.persist(stat);

        assertEquals(2, p.queryTotalStar(30013L, Subject.ENGLISH));

        stat = newInstance();
        stat.setUserId(30013L);
        stat.setStar(3);
        stat.setNewBookId("BK-103000001");
        p.persist(stat);

        assertEquals(5, p.queryTotalStar(30013L, Subject.ENGLISH));
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserRankStat.class)
    public void testUpdateStat() throws Exception {
        assertEquals(0, p.queryTotalStar(30013L, Subject.ENGLISH));
        assertEquals(0, p.queryByUserIdAndNewBookId(30013L, "BK-103000000").size());
        assertEquals(0, p.queryByUserIdAndNewBookId(30013L, "BK-103000001").size());

        AfentiLearningPlanUserRankStat stat = newInstance();
        stat.setUserId(30013L);
        stat.setNewBookId("BK-103000000");
        p.persist(stat);

        stat = newInstance();
        stat.setUserId(30013L);
        stat.setNewBookId("BK-103000001");
        p.persist(stat);

        List<AfentiLearningPlanUserRankStat> list1 = p.queryByUserIdAndNewBookId(30013L, "BK-103000000");
        assertEquals(1, list1.size());
        AfentiLearningPlanUserRankStat stat1 = list1.get(0);
        assertEquals(1, stat1.getStar().intValue());
        assertEquals(1, stat1.getSilver().intValue());
        assertEquals(1, stat1.getSuccessiveSilver().intValue());
        assertEquals(0, stat1.getBonus().intValue());

        List<AfentiLearningPlanUserRankStat> list2 = p.queryByUserIdAndNewBookId(30013L, "BK-103000001");
        assertEquals(1, list2.size());
        AfentiLearningPlanUserRankStat stat2 = list2.get(0);
        assertEquals(1, stat2.getStar().intValue());
        assertEquals(1, stat2.getSilver().intValue());
        assertEquals(1, stat2.getSuccessiveSilver().intValue());
        assertEquals(0, stat2.getBonus().intValue());

        assertEquals(2, p.queryTotalStar(30013L, Subject.ENGLISH));

        p.updateStat(stat1.getId(), stat1.getUserId(), stat1.getNewBookId(), Subject.ENGLISH, 1, 4, 9, 10);
        p.updateStat(stat2.getId(), stat2.getUserId(), stat2.getNewBookId(), Subject.ENGLISH, 2, 9, 14, 15);

        list1 = p.queryByUserIdAndNewBookId(30013L, "BK-103000000");
        assertEquals(1, list1.size());
        stat1 = list1.get(0);
        assertEquals(2, stat1.getStar().intValue());
        assertEquals(5, stat1.getSilver().intValue());
        assertEquals(10, stat1.getSuccessiveSilver().intValue());
        assertEquals(10, stat1.getBonus().intValue());

        list2 = p.queryByUserIdAndNewBookId(30013L, "BK-103000001");
        assertEquals(1, list2.size());
        stat2 = list2.get(0);
        assertEquals(3, stat2.getStar().intValue());
        assertEquals(10, stat2.getSilver().intValue());
        assertEquals(15, stat2.getSuccessiveSilver().intValue());
        assertEquals(15, stat2.getBonus().intValue());

        assertEquals(5, p.queryTotalStar(30013L, Subject.ENGLISH));
    }

    private AfentiLearningPlanUserRankStat newInstance() {
        AfentiLearningPlanUserRankStat stat = new AfentiLearningPlanUserRankStat();
        stat.setCreateTime(new Date());
        stat.setUpdateTime(new Date());
        stat.setUserId(0L);
        stat.setNewBookId("1");
        stat.setNewUnitId("11");
        stat.setRank(1);
        stat.setStar(1);
        stat.setSilver(1);
        stat.setSuccessiveSilver(1);
        stat.setBonus(0);
        stat.setSubject(Subject.ENGLISH);
        return stat;
    }
}
