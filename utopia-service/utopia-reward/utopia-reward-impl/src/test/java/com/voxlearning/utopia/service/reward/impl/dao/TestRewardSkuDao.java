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
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardSku.class)
public class TestRewardSkuDao {

    @Inject private RewardSkuDao rewardSkuDao;

    @Test
    public void testLoadByProductIds() throws Exception {
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        for (Long productId : productIds) {
            for (int i = 0; i < 3; i++) {
                RewardSku sku = new RewardSku();
                sku.setProductId(productId);
                sku.setSkuName("");
                rewardSkuDao.insert(sku);
            }
        }
        Map<Long, List<RewardSku>> map = rewardSkuDao.findByProductIds(productIds);
        assertEquals(productIds.size(), map.size());
        for (Long productId : productIds) {
            assertEquals(3, map.get(productId).size());
        }
    }

    @Test
    public void tetIncDec() throws Exception {
        RewardSku sku = new RewardSku();
        sku.setSkuName("");
        sku.setProductId(0L);
        rewardSkuDao.insert(sku);
        Long id = sku.getId();

        rewardSkuDao.increaseInventorySellable(id, 1);
        assertEquals(1, rewardSkuDao.load(id).getInventorySellable().intValue());
        rewardSkuDao.increaseInventorySellable(id, 1);
        assertEquals(2, rewardSkuDao.load(id).getInventorySellable().intValue());

        rewardSkuDao.decreaseInventorySellable(id, 2);
        assertEquals(0, rewardSkuDao.load(id).getInventorySellable().intValue());
        rewardSkuDao.decreaseInventorySellable(id, 1);
        assertEquals(0, rewardSkuDao.load(id).getInventorySellable().intValue());

    }
}
