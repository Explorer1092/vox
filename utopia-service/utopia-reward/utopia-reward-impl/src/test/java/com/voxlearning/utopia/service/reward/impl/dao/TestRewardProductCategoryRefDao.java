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
import com.voxlearning.alps.test.util.UnitTestUtils;
import com.voxlearning.utopia.service.reward.entity.RewardProductCategoryRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardProductCategoryRef.class)
public class TestRewardProductCategoryRefDao {

    @Inject private RewardProductCategoryRefDao rewardProductCategoryRefDao;

    @Test
    public void testFindByCategoryId() throws Exception {
        long categoryId = UnitTestUtils.nextId();
        for (int i = 0; i < 3; i++) {
            RewardProductCategoryRef document = new RewardProductCategoryRef();
            document.setCategoryId(categoryId);
            document.setProductId(UnitTestUtils.nextId());
            rewardProductCategoryRefDao.insert(document);
            assertEquals(i + 1, rewardProductCategoryRefDao.findByCategoryId(categoryId).size());
        }
    }

    @Test
    public void testFindByProductId() throws Exception {
        long productId = UnitTestUtils.nextId();
        for (int i = 0; i < 3; i++) {
            RewardProductCategoryRef document = new RewardProductCategoryRef();
            document.setCategoryId(UnitTestUtils.nextId());
            document.setProductId(productId);
            rewardProductCategoryRefDao.insert(document);
            assertEquals(i + 1, rewardProductCategoryRefDao.findByProductId(productId).size());
        }
    }
}
