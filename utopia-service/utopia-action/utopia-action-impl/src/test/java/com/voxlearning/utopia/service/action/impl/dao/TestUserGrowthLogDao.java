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

package com.voxlearning.utopia.service.action.impl.dao;

import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.document.UserGrowthLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserGrowthLogDao {
    @Inject
    private UserGrowthLogDao userGrowthLogDao;

    @Test
    public void testFindByUserId() throws Exception {
        for (int i = 0; i < 3; i++) {
            UserGrowthLog log = new UserGrowthLog();
            log.setUserId(30009L);
            log.setActionTime(new Date());
            String day = DayRange.newInstance(log.getActionTime().getTime()).toString();
            String id = "30009-" + day + "-" + RandomUtils.nextObjectId();
            log.setId(id);
            userGrowthLogDao.insert(log);
            assertEquals(i + 1, userGrowthLogDao.findByUserId(30009L).size());
        }

    }
}
