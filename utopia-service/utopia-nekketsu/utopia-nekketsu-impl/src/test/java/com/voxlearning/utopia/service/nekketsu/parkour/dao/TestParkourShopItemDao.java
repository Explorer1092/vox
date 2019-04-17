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

package com.voxlearning.utopia.service.nekketsu.parkour.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourShopItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestParkourShopItemDao {

    @Inject private ParkourShopItemDao parkourShopItemDao;

    @Test
    public void testParkourShopItemDao() throws Exception {
        List<ParkourShopItem> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.add(new ParkourShopItem());
        }
        parkourShopItemDao.replaceAll(list);
        assertEquals(3, parkourShopItemDao.loadWithCache().size());

        list.clear();
        for (int i = 0; i < 5; i++) {
            list.add(new ParkourShopItem());
        }
        parkourShopItemDao.replaceAll(list);
        assertEquals(5, parkourShopItemDao.loadWithCache().size());
    }
}
