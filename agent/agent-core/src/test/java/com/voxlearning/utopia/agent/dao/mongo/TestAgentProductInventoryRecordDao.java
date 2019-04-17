package com.voxlearning.utopia.agent.dao.mongo;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.AgentProductInventoryRecord;
import org.junit.Test;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

/**
 * TestAgentProductInventoryRecordDao
 *
 * @author song.wang
 * @date 2016/11/18
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestAgentProductInventoryRecordDao {
    @Inject private AgentProductInventoryRecordDao agentProductInventoryRecordDao;

    @Test
    public void testInsert(){
        AgentProductInventoryRecord inventoryRecord = new AgentProductInventoryRecord();
        inventoryRecord.setUserId(1L);
        inventoryRecord.setUserName("wang");
        inventoryRecord.setProductId(5L);
        inventoryRecord.setProductName("金豆子");
        inventoryRecord.setPreQuantity(5);
        inventoryRecord.setAfterQuantity(10);
        inventoryRecord.setQuantityChange(5);
        inventoryRecord.setComment("增加库存量5件");
        agentProductInventoryRecordDao.insert(inventoryRecord);
    }
}
