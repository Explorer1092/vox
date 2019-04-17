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

import com.voxlearning.alps.spi.test.RebuildDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotteryHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestCampaignLotteryHistoryDao {

    @Inject
    private CampaignLotteryHistoryDao campaignLotteryHistoryDao;

    @Test
    @RebuildDatabaseTable(
            tableTemplate = "VOX_CAMPAIGN_LOTTERY_HISTORY_{}",
            arguments = {"10", "20", "30"}
    )
    public void testFindCampaignLotteryHistories() throws Exception {
        List<Long> userIds = Arrays.asList(30010L, 30020L, 30030L);
        for (Long userId : userIds) {
            for (int i = 0; i < 3; i++) {
                CampaignLotteryHistory document = new CampaignLotteryHistory();
                document.setUserId(userId);
                document.setCampaignId(0);
                campaignLotteryHistoryDao.insert(document);
            }
        }
        for (Long userId : userIds) {
            assertEquals(3, campaignLotteryHistoryDao.findCampaignLotteryHistories(0, userId).size());
        }
    }
}
