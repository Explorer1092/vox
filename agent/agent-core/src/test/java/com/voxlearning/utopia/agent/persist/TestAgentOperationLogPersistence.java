package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.AgentOperationLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentOperationLog.class)
public class TestAgentOperationLogPersistence {

    @Inject private AgentOperationLogPersistence agentOperationLogPersistence;

    @Test
    public void testAgentOperationLogPersistence() throws Exception {
        AgentOperationLog log = new AgentOperationLog();
        log.setOperatorId(0L);
        log.setOperatorName("");
        log.setOperationType("");
        agentOperationLogPersistence.insert(log);
        assertNotNull(agentOperationLogPersistence.load(log.getId()));
    }
}
