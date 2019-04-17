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
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentUser.class)
public class TestAgentUserPersistence {

    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    @Inject private AgentUserServiceClient agentUserServiceClient;

    @Test
    public void testAgentUserPersistence() throws Exception {
        AgentUser document = new AgentUser();
        document.setAccountName("UTOPIA");
        document.setRealName("VOXLEARNING");
        document.setPasswd("");
        document.setPasswdSalt("");
        document.setCashAmount(0F);
        document.setPointAmount(0F);
        document.setUsableCashAmount(0F);
        document.setUsablePointAmount(0F);
        document.setTel("13800100100");
        agentUserServiceClient.persist(document);
        Long id = document.getId();
        assertEquals(1, agentUserLoaderClient.findAll().size());
        assertNotNull(agentUserLoaderClient.findByName("UTOPIA"));
        assertNotNull(agentUserLoaderClient.findByMobile("13800100100"));
        assertEquals(1, agentUserLoaderClient.findByRealName("LEARN").size());
        assertEquals(1, agentUserServiceClient.delete(id));
        assertEquals(9, agentUserLoaderClient.load(id).getStatus().intValue());
    }
}
