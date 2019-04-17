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
import com.voxlearning.utopia.agent.persist.entity.AgentUserAccountHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentUserAccountHistory.class)
public class TestAgentUserAccountHistoryPersistence {

    @Inject private AgentUserAccountHistoryPersistence agentUserAccountHistoryPersistence;

    @Test
    public void testFindByUserId() throws Exception {
        for (int i = 0; i < 3; i++) {
            AgentUserAccountHistory document = new AgentUserAccountHistory();
            document.setUserId(1L);
            document.setCashBefore(0F);
            document.setCashAmount(0F);
            document.setCashAfter(0F);
            document.setPointBefore(0F);
            document.setPointAmount(0F);
            document.setPointAfter(0F);
            agentUserAccountHistoryPersistence.insert(document);
            assertEquals(i + 1, agentUserAccountHistoryPersistence.findByUserId(1L).size());
        }
    }
}
