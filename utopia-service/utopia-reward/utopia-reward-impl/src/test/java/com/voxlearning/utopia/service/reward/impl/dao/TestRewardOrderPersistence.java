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

package com.voxlearning.utopia.service.reward.impl.dao;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by XiaoPeng.Yang on 14-7-14.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestRewardOrderPersistence {
    @Inject private RewardOrderPersistence rewardOrderPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = RewardOrder.class)
    public void testUpdateOrderStatus() throws Exception {
        RewardOrder order = new RewardOrder();
        order.setCreateDatetime(DateUtils.calculateDateDay(new Date(), -2));
        order.setProductId(1L);
        order.setProductName("111");
        order.setDisabled(false);
        order.setBuyerId(10002L);
        order.setBuyerName("老罗");
        order.setDiscount(1.0);
        order.setPrice(1.0);
        order.setQuantity(1);
        order.setSaleGroup("vip");
        order.setSkuId(11L);
        order.setSkuName(":");
        order.setStatus(RewardOrderStatus.SUBMIT.name());
        order.setTotalPrice(111.0);
        order.setUnit("sds");
        order.setCode("ff112233");
        rewardOrderPersistence.insert(order);
        Long id = order.getId();
        order = rewardOrderPersistence.load(id);
        assertEquals(RewardOrderStatus.SUBMIT.name(), order.getStatus());
        order = rewardOrderPersistence.load(id);
        assertEquals(RewardOrderStatus.SUBMIT.name(), order.getStatus());
    }
}
