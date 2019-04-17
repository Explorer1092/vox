package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.crm.CrmUserFollow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Yuechen.Wang on 2016/7/28.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestCrmUserFollowDao {
    @Inject CrmUserFollowDao crmUserFollowDao;

    @Test
    public void testFindByFollower() throws Exception {
        CrmUserFollow mock = new CrmUserFollow();
        mock.setFollowerId(1L);
        mock.setTarget("A");
        mock.setDisabled(false);
        crmUserFollowDao.insert(mock);

        mock = new CrmUserFollow();
        mock.setFollowerId(1L);
        mock.setTarget("B");
        mock.setDisabled(false);
        crmUserFollowDao.insert(mock);

        mock = new CrmUserFollow();
        mock.setFollowerId(1L);
        mock.setTarget("C");
        mock.setDisabled(true);
        crmUserFollowDao.insert(mock);

        assertEquals(2, crmUserFollowDao.findByFollower(1L).size());

    }

    @Test
    public void testFindByFollowerAndTarget() throws Exception {
        CrmUserFollow mock = new CrmUserFollow();
        mock.setFollowerId(1L);
        mock.setTarget("1");
        mock.setFollowType("SCHOOL");
        mock.setDisabled(false);
        crmUserFollowDao.insert(mock);

        assertNotNull(crmUserFollowDao.findByFollowerAndTarget(1L, 1L, "SCHOOL"));
    }
}