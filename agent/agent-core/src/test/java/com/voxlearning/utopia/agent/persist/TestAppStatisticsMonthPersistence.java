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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.agent.persist.entity.statistics.AppStatisticsMonth;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AppStatisticsMonth.class)
public class TestAppStatisticsMonthPersistence {
    @Inject private AppStatisticsMonthPersistence appStatisticsMonthPersistence;

    @Test
    public void testAppStatisticsMonthPersistence() throws Exception {
        AppStatisticsMonth document = new AppStatisticsMonth();
        document.setMonth(0);
        document.setAppKey("");
        appStatisticsMonthPersistence.insert(document);
        Long id = document.getId();
        assertNotNull(appStatisticsMonthPersistence.load(id));
    }
}
