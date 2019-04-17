/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.business.api.entity.RSRegionPatternStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Changyuan on 2015/1/13.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSRegionPatternStatDao {
    @Autowired private RSRegionPatternStatDao rsRegionPatternStatDao;

    @Test
    public void testFindByCityCode() throws Exception {
        long cityCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSRegionPatternStat rsRegionPatternStat = new RSRegionPatternStat();
            rsRegionPatternStat.setAcode(1L);
            rsRegionPatternStat.setAreaName("Test Area");
            rsRegionPatternStat.setCcode(cityCode);
            rsRegionPatternStat.setCityName("Test City");
            rsRegionPatternStat.setCreateAt(new Date());
            rsRegionPatternStat.setPatterns(null);
            rsRegionPatternStatDao.save(rsRegionPatternStat);

            if (i % 2 == 0) {
                rsRegionPatternStatDao.save(rsRegionPatternStat, 2014, Term.下学期);
            }
        }

        List<RSRegionPatternStat> rsRegionPatternStats = rsRegionPatternStatDao.findByCityCode(cityCode);
        assertEquals(10, rsRegionPatternStats.size());

        rsRegionPatternStats = rsRegionPatternStatDao.findByCityCode(cityCode, 2014, Term.下学期);
        assertEquals(5, rsRegionPatternStats.size());
    }

    @Test
    public void testFindByCityCodes() throws Exception {
        List<Long> cityCodes = new ArrayList<>();
        cityCodes.add(110100L);
        cityCodes.add(110101L);
        for (Long cityCode : cityCodes) {
            for (long i = 1; i <= 5; i++) {
                RSRegionPatternStat rsRegionPatternStat = new RSRegionPatternStat();
                rsRegionPatternStat.setAcode(1L);
                rsRegionPatternStat.setAreaName("Test Area");
                rsRegionPatternStat.setCcode(cityCode);
                rsRegionPatternStat.setCityName("Test City");
                rsRegionPatternStat.setCreateAt(new Date());
                rsRegionPatternStat.setPatterns(null);
                rsRegionPatternStatDao.save(rsRegionPatternStat);

                if (i % 2 == 0) {
                    rsRegionPatternStatDao.save(rsRegionPatternStat, 2014, Term.下学期);
                }
            }
        }

        List<RSRegionPatternStat> rsRegionPatternStats = rsRegionPatternStatDao.findByCityCodes(cityCodes);
        assertEquals(10, rsRegionPatternStats.size());

        rsRegionPatternStats = rsRegionPatternStatDao.findByCityCodes(cityCodes, 2014, Term.下学期);
        assertEquals(4, rsRegionPatternStats.size());
    }
}
