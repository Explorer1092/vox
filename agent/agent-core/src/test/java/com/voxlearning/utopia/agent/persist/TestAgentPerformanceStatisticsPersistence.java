package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.AgentPerformanceStatistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * TestAgentPerformanceStatisticsPersistence
 *
 * @author song.wang
 * @date 2017/3/28
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentPerformanceStatistics.class)
public class TestAgentPerformanceStatisticsPersistence {

    @Inject
    private AgentPerformanceStatisticsPersistence agentPerformanceStatisticsPersistence;

    @Test
    public void testDelete() throws Exception {

    }

}
