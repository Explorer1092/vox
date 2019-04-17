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
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentGroupServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentGroup.class)
public class TestAgentGroupPersistence {

    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject private AgentGroupServiceClient agentGroupServiceClient;

    @Test
    public void testFindByParentId() throws Exception {
        for (int i = 0; i < 3; i++) {
            AgentGroup agentGroup = new AgentGroup();
            agentGroup.setParentId(1L);
            agentGroup.setGroupName("");
            agentGroup.setRoleId(0);
            agentGroupServiceClient.persist(agentGroup);
            assertEquals(i + 1, agentGroupLoaderClient.findByParentId(1L).size());
        }
        assertEquals(3, agentGroupLoaderClient.findAllGroups().size());
    }

    @Test
    public void testFindByRoleId() throws Exception {
        for (int i = 0; i < 3; i++) {
            AgentGroup agentGroup = new AgentGroup();
            agentGroup.setParentId(0L);
            agentGroup.setGroupName("");
            agentGroup.setRoleId(1);
            agentGroupServiceClient.persist(agentGroup);
            assertEquals(i + 1, agentGroupLoaderClient.findByRoleId(1).size());
        }
        assertEquals(3, agentGroupLoaderClient.findAllGroups().size());
    }

    @Test
    public void testDelete() throws Exception {
        AgentGroup agentGroup = new AgentGroup();
        agentGroup.setParentId(0L);
        agentGroup.setGroupName("");
        agentGroup.setRoleId(0);
        agentGroupServiceClient.persist(agentGroup);
        Long id = agentGroup.getId();
        assertEquals(1, agentGroupLoaderClient.findAllGroups().size());
        assertEquals(0, agentGroupLoaderClient.findAllGroups().size());
    }
}
