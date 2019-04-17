/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserSchoolServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentUserSchool.class)
public class TestAgentUserSchoolPersistence {
    @Inject private AgentUserSchoolServiceClient agentUserSchoolServiceClient;
    @Inject private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;

    @Test
    public void testAgentUserSchoolPersistence() throws Exception {
        AgentUserSchool document = new AgentUserSchool();
        document.setUserId(0L);
        document.setRegionCode(0);
        document.setSchoolId(0L);
        document.setSchoolLevel(0);
        agentUserSchoolServiceClient.persist(document);
    }

    @Test
    public void testAgentUserSchoolPersistenceGetByUserIds() {
        AgentUserSchool user1 = new AgentUserSchool();
        user1.setRegionCode(111);
        user1.setSchoolId(1L);
        user1.setSchoolLevel(1);
        user1.setUserId(1L);
        user1.setDisabled(false);
        AgentUserSchool user2 = new AgentUserSchool();
        user2.setRegionCode(111);
        user2.setSchoolId(3L);
        user2.setSchoolLevel(1);
        user2.setUserId(2L);
        user2.setDisabled(false);
        AgentUserSchool user3 = new AgentUserSchool();
        AgentUserSchool user4 = new AgentUserSchool();
        AgentUserSchool user5 = new AgentUserSchool();
        user3.setRegionCode(111);
        user3.setSchoolId(3L);
        user3.setSchoolLevel(1);
        user3.setUserId(3L);
        user3.setDisabled(false);
        user4.setRegionCode(111);
        user4.setSchoolId(3L);
        user4.setSchoolLevel(1);
        user4.setUserId(4L);
        user4.setDisabled(false);
        user5.setRegionCode(111);
        user5.setSchoolId(3L);
        user5.setSchoolLevel(1);
        user5.setUserId(5L);
        user5.setDisabled(false);
        agentUserSchoolServiceClient.persist(user1);
        agentUserSchoolServiceClient.persist(user2);
        agentUserSchoolServiceClient.persist(user3);
        agentUserSchoolServiceClient.persist(user4);
        agentUserSchoolServiceClient.persist(user5);
        List<Long> userIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        Map<Long, List<AgentUserSchool>> userSchool = agentUserSchoolLoaderClient.findByUserIds(userIds);
        System.out.println("---------------------------------------------");
        userSchool.entrySet().forEach(p->System.out.println(p.getValue()));
        System.out.println("---------------------------------------------");
    }
}
