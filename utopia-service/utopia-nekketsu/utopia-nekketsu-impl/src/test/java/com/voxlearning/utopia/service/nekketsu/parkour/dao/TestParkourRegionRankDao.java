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
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourRegionRank;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestParkourRegionRankDao {
    @Inject private ParkourRegionRankDao parkourRegionRankDao;

    @Test
    public void testParkourRegionRankDao() {
        List<Integer> regions = Arrays.asList(10000, 20000, 30000);
        int stageId = 13;

        regions.stream()
                .map(t -> t + "_" + stageId)
                .map(t -> {
                    ParkourRegionRank inst = new ParkourRegionRank();
                    inst.setId(t);
                    return inst;
                })
                .forEach(parkourRegionRankDao::insert);

        Map<Integer, ParkourRegionRank> map = parkourRegionRankDao.getsRegionRankBatch(regions, stageId);
        assertEquals(regions.size(), map.size());
        regions.forEach(t -> assertTrue(map.containsKey(t)));
    }

}
