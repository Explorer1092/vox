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
import com.voxlearning.utopia.service.reward.entity.RewardProductTagRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardProductTagRef.class)
public class TestRewardProductTagRefDao {

    @Inject private RewardProductTagRefDao rewardProductTagRefDao;

    @Test
    public void testRewardProductTagRefDao() throws Exception {
        RewardProductTagRef document = new RewardProductTagRef();
        document.setProductId(1L);
        document.setTagId(2L);
        rewardProductTagRefDao.insert(document);

        assertEquals(1, rewardProductTagRefDao.findByProductId(1L).size());
        assertEquals(1, rewardProductTagRefDao.findByTagId(2L).size());

        assertEquals(1, rewardProductTagRefDao.deleteByProductId(1L));

        assertEquals(0, rewardProductTagRefDao.findByProductId(1L).size());
        assertEquals(0, rewardProductTagRefDao.findByTagId(2L).size());
    }
}
