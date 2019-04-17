package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reward.entity.RewardLogistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardLogistics.class)
public class TestRewardLogisticsPersistence {

    @Inject private RewardLogisticsPersistence rewardLogisticsPersistence;

    @Test
    public void testLoadRewardLogisticsBySchoolIdAndType() throws Exception {
        RewardLogistics logistics = new RewardLogistics();
        logistics.setSchoolId(1L);
        logistics.setType(RewardLogistics.Type.TEACHER);
        rewardLogisticsPersistence.insert(logistics);
        assertEquals(1, rewardLogisticsPersistence.loadRewardLogisticsBySchoolIdAndType(1L, RewardLogistics.Type.TEACHER).size());
    }

    @Test
    public void testLoadRewardLogisticsBySchoolIdAndMonthAndType() throws Exception {
        RewardLogistics logistics = new RewardLogistics();
        logistics.setSchoolId(1L);
        logistics.setType(RewardLogistics.Type.TEACHER);
        logistics.setMonth("201706");
        rewardLogisticsPersistence.insert(logistics);
        assertNotNull(rewardLogisticsPersistence.loadRewardLogisticsBySchoolIdAndMonthAndType(1L, "201706", RewardLogistics.Type.TEACHER));
    }

    @Test
    public void testLoadRewardLogisticsByMonthAndType() throws Exception {
        RewardLogistics logistics = new RewardLogistics();
        logistics.setType(RewardLogistics.Type.TEACHER);
        logistics.setMonth("201706");
        rewardLogisticsPersistence.insert(logistics);
        assertEquals(1, rewardLogisticsPersistence.loadRewardLogisticsByMonthAndType("201706", RewardLogistics.Type.TEACHER).size());
    }

    @Test
    public void testLoadByMonth() throws Exception {
        RewardLogistics logistics = new RewardLogistics();
        logistics.setMonth("201706");
        rewardLogisticsPersistence.insert(logistics);
        assertEquals(1, rewardLogisticsPersistence.loadByMonth("201706").size());
    }

    @Test
    public void testLoadByReceiverIdAndTypeAndMonth() throws Exception {
        RewardLogistics logistics = new RewardLogistics();
        logistics.setReceiverId(1L);
        logistics.setType(RewardLogistics.Type.TEACHER);
        logistics.setMonth("201706");
        rewardLogisticsPersistence.insert(logistics);
        assertNotNull(rewardLogisticsPersistence.loadByReceiverIdAndTypeAndMonth(1L, RewardLogistics.Type.TEACHER, "201706"));
    }
}
