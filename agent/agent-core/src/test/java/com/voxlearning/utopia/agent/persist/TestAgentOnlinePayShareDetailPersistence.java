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
import com.voxlearning.utopia.agent.persist.entity.AgentOnlinePayShareDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentOnlinePayShareDetail.class)
public class TestAgentOnlinePayShareDetailPersistence {
    @Inject private AgentOnlinePayShareDetailPersistence agentOnlinePayShareDetailPersistence;

    @Test
    public void testAgentOnlinePayShareDetailPersistence() throws Exception {
        AgentOnlinePayShareDetail document = new AgentOnlinePayShareDetail();
        document.setKpiEvalDate(new Date());
        document.setProductName("");
        document.setPayMonth(0);
        document.setTotalIncome(0D);
        document.setCardPayAmount(0D);
        document.setRefundAmount(0D);
        document.setOperationRate(0D);
        document.setShareableAmount(0D);
        document.setPayUserNum(0);
        document.setMonthlyActiveUsers(0);
        document.setMonthlyPayRate(0D);
        document.setUserId(0L);
        document.setShareAmount(0D);
        agentOnlinePayShareDetailPersistence.insert(document);
        Long id = document.getId();
        assertNotNull(agentOnlinePayShareDetailPersistence.load(id));
    }
}
