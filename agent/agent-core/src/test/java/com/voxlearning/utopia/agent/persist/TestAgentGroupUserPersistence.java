package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupUserServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Yuechen.Wang on 2016/7/28.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAgentGroupUserPersistence {
    @Inject
    AgentGroupUserLoaderClient agentGroupUserLoaderClient;

    @Inject
    AgentGroupUserServiceClient agentGroupUserServiceClient;

    @Test
    @TruncateDatabaseTable(databaseEntities = AgentGroupUser.class)
    public void testFindByGroupIds() throws Exception {
        AgentGroupUser mock = new AgentGroupUser();
        mock.setGroupId(1L);
        mock.setUserId(1L);
        agentGroupUserServiceClient.persist(mock);

        mock = new AgentGroupUser();
        mock.setGroupId(2L);
        mock.setUserId(2L);
        agentGroupUserServiceClient.persist(mock);

        assertEquals("数据查询结果", 2, agentGroupUserLoaderClient.findByGroupIds(Arrays.asList(1L, 2L)).size());
    }
}