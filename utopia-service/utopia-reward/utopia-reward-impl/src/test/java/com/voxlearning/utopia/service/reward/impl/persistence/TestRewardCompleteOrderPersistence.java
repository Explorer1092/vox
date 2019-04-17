/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reward.constant.RewardOrderSaleGroup;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.RewardCompleteOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * Created by XiaoPeng.Yang on 14-8-1.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardCompleteOrder.class)
public class TestRewardCompleteOrderPersistence {
    @Inject private RewardCompleteOrderPersistence rewardCompleteOrderPersistence;

    @Test
    public void testRewardCompleteOrderPersistence() throws Exception {
        RewardCompleteOrder order = new RewardCompleteOrder();
        order.setProductId(1L);
        order.setBuyerId(30013L);
        order.setBuyerName("");
        order.setDiscount(1.00);
        order.setPrice(12.00);
        order.setQuantity(1);
        order.setSaleGroup(RewardOrderSaleGroup.VIP.name());
        order.setSkuId(35L);
        order.setSkuName("红色");
        order.setTotalPrice(12.00);
        order.setUnit("学豆");
        order.setStatus(RewardOrderStatus.PREPARE.name());
        order.setReceiverId(0L);
        order.setReceiverName("");
        rewardCompleteOrderPersistence.insert(order);
        order = rewardCompleteOrderPersistence.load(order.getId());
        assertEquals(30013L, order.getBuyerId().longValue());
        assertEquals(1, rewardCompleteOrderPersistence.updateRewardCompleteOrderLogisticsId(order.getId(), 999L));
        order = rewardCompleteOrderPersistence.load(order.getId());
        assertEquals("999", order.getLogisticsId());
        assertEquals(1, rewardCompleteOrderPersistence.updateCompleteOrderStatus(order.getId(), RewardOrderStatus.DELIVER));
        order = rewardCompleteOrderPersistence.load(order.getId());
        assertEquals("DELIVER", order.getStatus());
    }
}
