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
import com.voxlearning.utopia.service.campaign.api.document.CampaignLottery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = CampaignLottery.class)
public class TestCampaignLotteryDao {
    @Inject
    private CampaignLotteryDao campaignLotteryDao;

    @Test
    public void testFindByCampaignId() throws Exception {
        for (int i = 0; i < 3; i++) {
            CampaignLottery document = new CampaignLottery();
            document.setCampaignId(1);
            document.setAwardId(i + 1);
            document.setAwardLevelName("");
            document.setAwardName("");
            document.setAwardContent("");
            document.setAwardRate(0);
            campaignLotteryDao.insert(document);
            assertEquals(i + 1, campaignLotteryDao.findByCampaignId(1).size());
        }
    }
}
