package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.ApplyType;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by yaguang.wang
 * on 2017/3/17.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AgentOrder.class)
public class TestAgentOrderPersistence {
    @Inject
    private AgentOrderPersistence agentOrderPersistence;

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
        agentOrderPersistence.insert(order);
        assertEquals(1, agentOrderPersistence.findByUser(SystemPlatformType.AGENT, "191").size());

        agentOrderPersistence.insert(order);
        assertEquals(2, agentOrderPersistence.findByUser(SystemPlatformType.AGENT, "191").size());
    }

}
