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

package com.voxlearning.utopia.service.campaign.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryBigHistory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = CampaignLotteryBigHistory.class)
public class TestCampaignLotteryBigHistoryDao {

    @Inject
    private CampaignLotteryBigHistoryDao campaignLotteryBigHistoryDao;

    @Test
    public void testLoadByCampaignId() throws Exception {
        for (int i = 0; i < 3; i++) {
            CampaignLotteryBigHistory history = new CampaignLotteryBigHistory();
            history.setUserId(0L);
            history.setCampaignId(1);
            campaignLotteryBigHistoryDao.insert(history);
            Assert.assertEquals(i + 1, campaignLotteryBigHistoryDao.loadByCampaignId(1).size());
        }
    }

    @Test
    public void testLoadByCampaignIdAndAwardId() throws Exception {
        for (int i = 0; i < 3; i++) {
            CampaignLotteryBigHistory history = new CampaignLotteryBigHistory();
            history.setUserId(0L);
            history.setCampaignId(1);
            history.setAwardId(2);
            campaignLotteryBigHistoryDao.insert(history);
            Assert.assertEquals(i + 1, campaignLotteryBigHistoryDao.loadByCampaignIdAndAwardId(1, 2).size());
        }
    }
}
