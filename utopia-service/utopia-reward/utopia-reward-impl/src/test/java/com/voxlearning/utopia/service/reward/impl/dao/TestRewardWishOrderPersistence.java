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

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.reward.entity.RewardWishOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardWishOrder.class)
public class TestRewardWishOrderPersistence {
    @Inject private RewardWishOrderPersistence rewardWishOrderPersistence;

    @Test
    public void testLoadByUserIds() throws Exception {
        List<Long> userIds = Arrays.asList(1L, 2L, 3L);
        Map<Long, List<RewardWishOrder>> map = rewardWishOrderPersistence.loadByUserIds(userIds);
        assertEquals(0, map.size());

        List<RewardWishOrder> orders = new LinkedList<>();
        for (Long userId : userIds) {
            for (int i = 0; i < 3; i++) {
                orders.add(RewardWishOrder.newInstance(0L, userId));
            }
        }
        rewardWishOrderPersistence.inserts(orders);

        map = rewardWishOrderPersistence.loadByUserIds(userIds);
        assertEquals(userIds.size(), map.size());
        for (Long userId : userIds) {
            assertEquals(3, map.get(userId).size());
        }
    }

    @Test
    public void testDeleteWishOrderById() throws Exception {
        RewardWishOrder wish = new RewardWishOrder();
        wish.setProductId(0L);
        wish.setUserId(1L);
        rewardWishOrderPersistence.insert(wish);
        Long id = wish.getId();
        rewardWishOrderPersistence.deleteWishOrderById(id, 1L);
        wish = rewardWishOrderPersistence.load(id);
        assertTrue(wish.getDisabled());
    }

    @Test
    public void testAchievedWishOrderById() throws Exception {
        RewardWishOrder wish = new RewardWishOrder();
        wish.setProductId(0L);
        wish.setUserId(0L);
        rewardWishOrderPersistence.insert(wish);
        Long id = wish.getId();
        rewardWishOrderPersistence.achievedWishOrderById(id);
        wish = rewardWishOrderPersistence.load(id);
        assertTrue(wish.getAchieved());
        assertNotNull(wish.getAchievedDatetime());
    }
}

