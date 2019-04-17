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
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardProduct.class)
public class TestRewardProductDao {

    @Inject private RewardProductDao rewardProductDao;

    @Test
    public void testWishQuantity() throws Exception {
        RewardProduct document = new RewardProduct();
        document.setProductName("");
        document.setProductType("");
        rewardProductDao.insert(document);
        Long id = document.getId();

        assertEquals(1, rewardProductDao.increaseWishQuantity(id, 3));
        document = rewardProductDao.load(id);
        assertEquals(3, document.getWishQuantity().intValue());
        assertEquals(1, rewardProductDao.increaseWishQuantity(id, 7));
        document = rewardProductDao.load(id);
        assertEquals(10, document.getWishQuantity().intValue());

        assertEquals(1, rewardProductDao.decreaseWishQuantity(id, 10));
        document = rewardProductDao.load(id);
        assertEquals(0, document.getWishQuantity().intValue());
        assertEquals(0, rewardProductDao.decreaseWishQuantity(id, 1));
    }

    @Test
    public void testSoldQuantity() throws Exception {
        RewardProduct document = new RewardProduct();
        document.setProductName("");
        document.setProductType("");
        rewardProductDao.insert(document);
        Long id = document.getId();

        assertEquals(1, rewardProductDao.increaseSoldQuantity(id, 3));
        document = rewardProductDao.load(id);
        assertEquals(3, document.getSoldQuantity().intValue());
        assertEquals(1, rewardProductDao.increaseSoldQuantity(id, 7));
        document = rewardProductDao.load(id);
        assertEquals(10, document.getSoldQuantity().intValue());

        assertEquals(1, rewardProductDao.decreaseSoldQuantity(id, 10));
        document = rewardProductDao.load(id);
        assertEquals(0, document.getSoldQuantity().intValue());
        assertEquals(0, rewardProductDao.decreaseSoldQuantity(id, 1));
    }
}
