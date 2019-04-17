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
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentOrderServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentOrder.class)
public class TestAgentOrderPersistence {
    @Inject
    private AgentOrderServiceClient agentOrderServiceClient;



    @Test
    public void findByUser() {
        AgentOrder order = new AgentOrder();
        order.setApplyType(ApplyType.AGENT_MATERIAL_APPLY);
        order.setUserPlatform(SystemPlatformType.AGENT);
        order.setAccount("191");
        order.setAccount("市经理");
        order.setStatus(ApplyStatus.PENDING);
        order.setCreatorName("191");
        order.setRealCreator("jajaja");
        order.setCreatorGroup(111L);
        order.setCreator(111L);
        order.setOrderType(1);
        order.setOrderAmount(1.0f);
        order.setPointChargeAmount(1.0f);
        order.setOrderNotes("1111111");
        order.setOrderStatus(1);
        order.setRealLatestProcessor("111");
        order.setLatestProcessor(1111L);
        order.setLatestProcessorGroup(111L);
        order.setLatestProcessorName("dasdasd");
        order.setConsignee("dasdasd");
        order.setAddress("dasdasd");
        order.setMobile("121212121");
        order.setLogisticsInfo("dasdasd");
        order.setInvoiceId(1111L);
        order.setOrderTime(new Date());
        order.setPaymentMode(11);
        order.setPaymentVoucher("asdasdas");
        order.setCityCostMonth(1);
        agentOrderServiceClient.insertAgentOrder(order);
    }

    /*@Inject private AgentOrderPersistence agentOrderPersistence;

    @Test
    public void testFindByCreator() throws Exception {
        AgentOrder order = new AgentOrder();
        order.setRealCreator("10000");
        order.setCreatorGroup(0L);
        order.setOrderType(0);
        order.setOrderAmount(0F);
        order.setOrderStatus(0);
        order.setRealLatestProcessor("UTOPIA");
        order.setLatestProcessorGroup(0L);
        agentOrderPersistence.insert(order);
        assertEquals(1, agentOrderPersistence.findByCreator(10000L).size());
    }

//    @Test
//    public void testFindByNote() throws Exception {
//        AgentOrder order = new AgentOrder();
//        order.setRealCreator("10000");
//        order.setCreatorGroup(0L);
//        order.setOrderType(0);
//        order.setOrderAmount(0F);
//        order.setOrderStatus(0);
//        order.setRealLatestProcessor("UTOPIA");
//        order.setLatestProcessorGroup(0L);
//        order.setOrderNotes("NOTES");
//        agentOrderPersistence.insert(order);
//        assertNotNull(agentOrderPersistence.findByNote("NOTES"));
//    }

    @Test
    public void testUpdateCrmUserInfo() throws Exception {
        AgentOrder order = new AgentOrder();
        order.setRealCreator("10000");
        order.setCreatorGroup(0L);
        order.setOrderType(0);
        order.setOrderAmount(0F);
        order.setOrderStatus(0);
        order.setRealLatestProcessor("UTOPIA");
        order.setLatestProcessorGroup(0L);
        agentOrderPersistence.insert(order);
        Long id = order.getId();
        List<AgentOrder> list = agentOrderPersistence.findByCreator(10000L);
        assertEquals(1, list.size());
        assertEquals("UTOPIA", list.get(0).getRealLatestProcessor());
        assertTrue(agentOrderPersistence.updateCrmUserInfo(id, "20000", "ALPS"));
        list = agentOrderPersistence.findByCreator(10000L);
        assertEquals(0, list.size());
        list = agentOrderPersistence.findByCreator(20000L);
        assertEquals(1, list.size());
        assertEquals("ALPS", list.get(0).getRealLatestProcessor());
    }*/
}
