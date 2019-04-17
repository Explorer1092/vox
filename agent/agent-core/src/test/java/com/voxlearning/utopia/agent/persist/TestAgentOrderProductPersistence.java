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
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrderProduct;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentOrderProduct.class)
public class TestAgentOrderProductPersistence {
    /*@Inject private AgentOrderProductPersistence agentOrderProductPersistence;

    @Test
    public void testAgentOrderProductPersistence() throws Exception {
        AgentOrderProduct document = new AgentOrderProduct();
        document.setOrderId(0L);
        document.setProductId(0L);
        document.setRank(0);
        agentOrderProductPersistence.insert(document);
        assertNotNull(agentOrderProductPersistence.load(document.getId()));
    }*/
}
