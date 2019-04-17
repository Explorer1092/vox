package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.AgentMonthly;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Administrator on 2016/8/18.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAgentMonthlyDao {
    @Inject private AgentMonthlyDao agentMonthlyDao;

    @Test
    public void findByUserAndDay() throws Exception {
        AgentMonthly mock = new AgentMonthly();
        mock.setMonth(1);
        mock.setUserId(1L);
        agentMonthlyDao.insert(mock);

        assertNotNull(agentMonthlyDao.findByUserAndMonth(1L, 1));

        FlightRecorder.dot("Test Cache");
        String key = AgentMonthly.ck_uid_month(1L, 1);
        CacheObject<AgentMonthly> cacheObject = agentMonthlyDao.getCache().get(key);
        assertNotNull(cacheObject.getValue());
    }

}