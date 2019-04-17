package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 *
 *
 * @author song.wang
 * @date 2016/7/29
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAgentNeedFollowUpDao {
    @Inject
    private AgentNeedFollowUpDao agentNeedFollowUpDao;


    @Test
    public void testFindByWorker() throws Exception {


    }
}
