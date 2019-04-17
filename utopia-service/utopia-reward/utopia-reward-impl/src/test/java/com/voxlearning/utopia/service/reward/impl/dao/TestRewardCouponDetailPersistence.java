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
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

/**
 * Created by XiaoPeng.Yang on 14-7-30.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = RewardCouponDetail.class)
public class TestRewardCouponDetailPersistence {
    @Autowired
    private RewardCouponDetailPersistence rewardCouponDetailPersistence;

    @Test
    public void testCouponExchanged() throws Exception {
        RewardCouponDetail document = new RewardCouponDetail();
        document.setCouponNo("");
        document.setProductId(0L);
        rewardCouponDetailPersistence.insert(document);
        Long id = document.getId();
        rewardCouponDetailPersistence.couponExchanged(document, 1L, "13800100100");
        document = rewardCouponDetailPersistence.load(id);
        assertEquals(1, document.getUserId().longValue());
        assertEquals("13800100100", document.getSensitiveMobile());
        assertTrue(document.getExchanged());
        assertNotNull(document.getExchangedDate());
    }
}
