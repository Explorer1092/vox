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
import com.voxlearning.utopia.agent.persist.entity.statistics.AppSticky;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AppSticky.class)
public class TestAppStickyPersistence {

    @Inject private AppStickyPersistence appStickyPersistence;

    @Test
    public void testAppStickyPersistence() throws Exception {
        AppSticky document = new AppSticky();
        document.setAppKey("");
        document.setBeginDay(0);
        document.setCurrentDay(0);
        document.setUserCount(0);
        appStickyPersistence.insert(document);
        Long id = document.getId();
        assertNotNull(appStickyPersistence.load(id));
    }
}