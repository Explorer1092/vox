package com.voxlearning.utopia.service.action.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.document.ClazzAchievementLog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

/**
 * @author xinxin
 * @since 26/8/2016
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestClazzAchievementLogDao {
    @Inject
    private ClazzAchievementLogDao clazzAchievementLogDao;

    @Test
    public void testFindByClazzId() {
        ClazzAchievementLog log = new ClazzAchievementLog();
        log.setId(2003+"-ShenSuanZi-1");
        log.setUserId(30008L);
        log.setClazzId(2003L);
        log.setAchievementLevel(1);
        log.setAchievementType("ShenSuanZi");

        clazzAchievementLogDao.insert(log);

        log = new ClazzAchievementLog();
        log.setId(2003+"-ShenSuanZi-2");
        log.setUserId(30008L);
        log.setClazzId(2003L);
        log.setAchievementLevel(2);
        log.setAchievementType("ShenSuanZi");

        clazzAchievementLogDao.insert(log);

        List<ClazzAchievementLog> byClazzId = clazzAchievementLogDao.findByClazzId(2003L);

        Assert.assertEquals(2,byClazzId.size());
    }
}
