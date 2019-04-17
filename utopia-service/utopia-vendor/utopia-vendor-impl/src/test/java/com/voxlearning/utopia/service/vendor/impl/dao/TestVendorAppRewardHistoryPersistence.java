/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.vendor.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppRewardHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by tanguohong on 14-1-17.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestVendorAppRewardHistoryPersistence {
    @Inject VendorAppRewardHistoryPersistence vendorAppRewardHistoryPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = VendorAppRewardHistory.class)
    public void testFindByCreateTimeAndUserIdAndAppIdAndRewardType() throws Exception {

        Date createTime = new Date();

        Long userId = 1L;
        int appId = 1;
        String rewardType = "pk";
        VendorAppRewardHistory vendorAppRewardHistoryPk = new VendorAppRewardHistory();
        vendorAppRewardHistoryPk.setRewardValue(1);
        vendorAppRewardHistoryPk.setAppId(appId);
        vendorAppRewardHistoryPk.setRewardType(rewardType);
        vendorAppRewardHistoryPk.setUserId(userId);
        vendorAppRewardHistoryPk.setComment("aaa");
        vendorAppRewardHistoryPersistence.insert(vendorAppRewardHistoryPk);
        List<VendorAppRewardHistory> vendorAppRewardHistories = vendorAppRewardHistoryPersistence.findByCreateTimeAndUserIdAndAppIdAndRewardType(createTime, userId, appId, rewardType);
        assertEquals(1, vendorAppRewardHistories.size());

    }

}
