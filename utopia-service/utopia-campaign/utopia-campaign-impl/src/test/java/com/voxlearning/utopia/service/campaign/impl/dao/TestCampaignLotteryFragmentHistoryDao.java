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
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryFragmentHistory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = CampaignLotteryFragmentHistory.class)
public class TestCampaignLotteryFragmentHistoryDao {

    @Inject
    private CampaignLotteryFragmentHistoryDao campaignLotteryFragmentHistoryDao;

    @Test
    public void testLoadByCampaignIdAndUserId() throws Exception {
        for (int i = 0; i < 3; i++) {
            CampaignLotteryFragmentHistory history = new CampaignLotteryFragmentHistory();
            history.setCampaignId(1);
            history.setUserId(30000L);
            campaignLotteryFragmentHistoryDao.insert(history);
            Assert.assertEquals(i + 1, campaignLotteryFragmentHistoryDao.loadByCampaignIdAndUserId(1, 30000L).size());
        }
    }
}
