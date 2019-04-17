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
import com.voxlearning.utopia.agent.persist.entity.AgentProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentProduct.class)
public class TestAgentProductPersistence {

    @Inject private AgentProductPersistence agentProductPersistence;

    @Test
    public void testDelete() throws Exception {
        AgentProduct product = new AgentProduct();
        product.setProductName("");
        product.setPrice(0F);
        product.setDiscountPrice(0F);
        product.setLatestEditor(0L);
        agentProductPersistence.insert(product);
        Long id = product.getId();
        assertNotNull(agentProductPersistence.load(id));
        assertEquals(1, agentProductPersistence.findAll().size());
        assertEquals(1, agentProductPersistence.delete(id));
        assertNull(agentProductPersistence.load(id));
        assertEquals(0, agentProductPersistence.findAll().size());
    }
}
