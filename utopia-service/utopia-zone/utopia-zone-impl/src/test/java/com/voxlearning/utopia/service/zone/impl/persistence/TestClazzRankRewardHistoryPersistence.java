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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankRewardHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ClazzRankRewardHistory.class)
public class TestClazzRankRewardHistoryPersistence {

    @Inject private ClazzRankRewardHistoryPersistence clazzRankRewardHistoryPersistence;

    @Test
    public void testClazzRankRewardHistoryPersistence() throws Exception {
        ClazzRankRewardHistory history = new ClazzRankRewardHistory();
        history.setUserId(10000L);
        history.setClazzId(100L);
        history.setMonth("201411");
        clazzRankRewardHistoryPersistence.insert(history);
        assertEquals(1, clazzRankRewardHistoryPersistence.findByUserId(10000L).size());
    }
}
