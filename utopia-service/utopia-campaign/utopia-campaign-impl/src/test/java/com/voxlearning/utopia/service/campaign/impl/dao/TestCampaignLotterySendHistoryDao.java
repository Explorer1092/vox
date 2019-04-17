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
import com.voxlearning.utopia.service.campaign.api.document.CampaignLotterySendHistory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = CampaignLotterySendHistory.class)
public class TestCampaignLotterySendHistoryDao {

    @Inject
    private CampaignLotterySendHistoryDao campaignLotterySendHistoryDao;

    @Test
    public void testCampaignLotterySendHistoryPersistence() throws Exception {
        CampaignLotterySendHistory history = new CampaignLotterySendHistory();
        history.setCampaignId(1);
        history.setReceiverId(2L);
        history.setSenderId(0L);
        history.setSenderName("");
        campaignLotterySendHistoryDao.insert(history);
        Assert.assertEquals(1, campaignLotterySendHistoryDao.loadByCampaignIdAndReceiverId(1, 2L).size());
    }
}
