package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.constants.AgentAppContentType;
import com.voxlearning.utopia.agent.constants.AgentDataPacketType;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * app内容管理测试
 * Created by yagaung.wang on 2016/8/3.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAgentAppContentPacketDao {
    @Inject private AgentAppContentPacketDao agentAppContentPacketDao;

    @Test
    public void testFindByContentType() {
        for (int t = 0; t < 2; t++) {
            AgentAppContentPacket dataPacket = new AgentAppContentPacket();
            dataPacket.setContentType(AgentAppContentType.DATA_PACKET);
            dataPacket.setDisabled(false);
            dataPacket.setFileUrl("jajajaja");
            dataPacket.setDatumType(AgentDataPacketType.POLICY_PAPER);

            AgentAppContentPacket dataPacket1 = new AgentAppContentPacket();
            dataPacket1.setContentType(AgentAppContentType.DATA_PACKET);
            dataPacket1.setDisabled(false);
            dataPacket1.setFileUrl("jajajaja1111");
            dataPacket1.setDatumType(AgentDataPacketType.LEADER_INFO);
            agentAppContentPacketDao.insert(dataPacket);
            agentAppContentPacketDao.insert(dataPacket1);
        }
        List<AgentAppContentPacket> res = agentAppContentPacketDao.findByContentType(AgentAppContentType.DATA_PACKET);
        assertEquals(4, res.size());
        List<AgentAppContentPacket> res1 = agentAppContentPacketDao.findByDatumType(AgentDataPacketType.LEADER_INFO);
        assertEquals(2, res1.size());
    }

    @Test
    public void testDeleteAppContentPacket() {
        for (int t = 0; t < 2; t++) {
            AgentAppContentPacket dataPacket = new AgentAppContentPacket();
            dataPacket.setContentType(AgentAppContentType.DATA_PACKET);
            dataPacket.setDisabled(false);
            dataPacket.setFileUrl("hahahaha");
            dataPacket.setDatumType(AgentDataPacketType.POLICY_PAPER);
            agentAppContentPacketDao.insert(dataPacket);
        }
        List<AgentAppContentPacket> res = agentAppContentPacketDao.findByContentType(AgentAppContentType.DATA_PACKET);
        assertEquals(2, res.size());
        agentAppContentPacketDao.deleteAgentAppContentPacket(res.get(0).getId());
        res = agentAppContentPacketDao.findByContentType(AgentAppContentType.DATA_PACKET);
        assertEquals(true, res.get(0).getDisabled());
        assertEquals(2, res.size());
    }
}
