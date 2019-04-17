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
import com.voxlearning.utopia.agent.persist.entity.AgentSysPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentSysPath.class)
public class TestAgentSysPathPersistence {

    @Inject private AgentSysPathPersistence agentSysPathPersistence;

    @Test
    public void testDelete() throws Exception {
        AgentSysPath path = new AgentSysPath();
        path.setAppName("");
        path.setPathName("");
        path.setDescription("");
        agentSysPathPersistence.insert(path);
        Long id = path.getId();
        assertNotNull(agentSysPathPersistence.load(id));
        assertEquals(1, agentSysPathPersistence.findAll().size());
        assertEquals(1, agentSysPathPersistence.delete(id));
        assertNull(agentSysPathPersistence.load(id));
        assertEquals(0, agentSysPathPersistence.findAll().size());
    }
}
