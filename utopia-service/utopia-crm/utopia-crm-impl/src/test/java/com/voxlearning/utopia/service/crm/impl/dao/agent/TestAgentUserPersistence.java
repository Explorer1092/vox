package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

/**
 * TestAgentUserPersistence
 *
 * @author song.wang
 * @date 2017/3/6
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentUser.class)
public class TestAgentUserPersistence {

    @Inject AgentUserPersistence agentUserPersistence;
    @Test
    public void testCustomizedPersist() throws Exception {
        List<AgentUser> userList = agentUserPersistence.findByRealName("王松市经理");
        if(CollectionUtils.isNotEmpty(userList)){
            userList.forEach(p -> System.out.println(p.getRealName()));
        }
    }
}
