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

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.action.api.document.UserGrowthRewardLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author xinxin
 * @since 10/8/2016
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestUserGrowthRewardLogDao {

    @Inject private UserGrowthRewardLogDao userGrowthRewardLogDao;

    @Test
    public void testFindByUserId() {
        long userId = 30033;
        for (int i = 0; i < 3; i++) {
            UserGrowthRewardLog document = new UserGrowthRewardLog();
            document.setId(userId + "-" + (i + 1));
            document.setUserId(userId);
            document.setGrowthLevel(i + 1);
            userGrowthRewardLogDao.insert(document);
            assertEquals(i + 1, userGrowthRewardLogDao.findByUserId(userId).size());
        }
    }
}
