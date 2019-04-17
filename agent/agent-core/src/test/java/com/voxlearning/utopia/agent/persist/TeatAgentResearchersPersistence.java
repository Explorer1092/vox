package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.AgentResearchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * 测试教研员
 * Created by yaguang.wang on 2016/10/19.
 */

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentResearchers.class)
public class TeatAgentResearchersPersistence {
    @Inject private AgentResearchersPersistence agentResearchersPersistence;

    @Test
    public void installAgentResearchers() {
        AgentResearchers agentResearchers = new AgentResearchers();
        agentResearchers.setAgentUserId(1111L);
        agentResearchers.setProvince(12345);
        agentResearchers.setCity(1);
        agentResearchers.setCounty(1);
        agentResearchers.setGender(1);
        agentResearchers.setGrade("一");
        agentResearchers.setJob(12);
        agentResearchers.setLevel(1);
        agentResearchers.setName("abckd");
        agentResearchers.setPhone("13190965222");
        agentResearchers.setSchoolPhase(1);
        agentResearchers.setSubject(Subject.CHINESE);
        agentResearchersPersistence.insert(agentResearchers);
        assertNotNull(agentResearchersPersistence.findAgentResearchersByUserId(1111L));
        assertEquals(1,agentResearchersPersistence.findAgentResearchersByUserId(1111L).size());
    }
}
