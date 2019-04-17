package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekSubordinateData;
import com.voxlearning.utopia.agent.persist.entity.AgentWeekly;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * TestAgentWeeklyDao
 *
 * @author song.wang
 * @date 2016/8/11
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAgentWeeklyDao {
    @Inject
    private AgentWeeklyDao agentWeeklyDao;

    @Test
    public void testInsert() throws Exception {
        AgentWeekly agentWeekly = new AgentWeekly();
        agentWeekly.setUserId(196L);
        agentWeekly.setDay(20160811);
        agentWeekly.setTitle("20160801--20160807");
        agentWeekly.setJuniorSascFloat(23.5);
        agentWeekly.setJuniorDascFloat(30.2);
        agentWeekly.setMiddleSascFloat(20d);
        agentWeekly.setJuniorSascCompleteRate(28.2);
        agentWeekly.setJuniorDascCompleteRate(25d);
        agentWeekly.setMiddleSascCompleteRate(21.5);
        agentWeekly.setRanking(6);
        agentWeekly.setPreWeekRanking(8);

        List<AgentWeekSubordinateData> subordinateDataList = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            AgentWeekSubordinateData data = new AgentWeekSubordinateData();
            data.setUserId(i * 10L);
            data.setUserName("下属" + i);
            data.setRankingFloat(4);
            List<Integer> unWorkedDayList = new ArrayList<>();
            unWorkedDayList.add(20160712 + 5 + i);
            unWorkedDayList.add(20160712 + 8 + i);
            unWorkedDayList.add(20160712 + 9 + i);
            data.setUnWorkedDayList(unWorkedDayList);
//            data.setQualityLevel(AgentWeekSubordinateData.QUALITY_LEVEL_BAD);
            subordinateDataList.add(data);
        }
        agentWeekly.setSubordinateDataList(subordinateDataList);

        agentWeeklyDao.insert(agentWeekly);

        AgentWeekly agentWeekly2 = agentWeeklyDao.findByUserAndDay(196L, 20160811);
        agentWeekly2.getId();
    }
}
