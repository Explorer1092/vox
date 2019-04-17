/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.nekketsu.adventure.impl.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.nekketsu.adventure.constant.GiftType;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.Gift;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestGiftDao {

    @Inject private GiftDao giftDao;

    @Test
    @MockBinder(
            type = Gift.class,
            jsons = {
                    "{'userId':1,'grant':false,'type':'STAGE'}",
                    "{'userId':1,'grant':false,'type':'STAGE'}",
                    "{'userId':1,'grant':false,'type':'STAGE'}",
            },
            persistence = GiftDao.class
    )
    public void testGetUngrantGifts() throws Exception {
        assertEquals(3, giftDao.getUngrantGifts(1L, GiftType.STAGE).size());
    }
}
