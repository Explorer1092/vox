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
import com.voxlearning.utopia.service.business.api.entity.RSRegionUnitWeakPointStat;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSBookData;
import com.voxlearning.utopia.service.business.api.entity.embedded.RSLevelData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Changyuan on 2015/1/15.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSRegionUnitWeakPointStatDao {
    @Autowired private RSRegionUnitWeakPointStatDao rsRegionUnitWeakPointStatDao;

    @Test
    public void testFindByCityCode() throws Exception {
        long cityCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSRegionUnitWeakPointStat rsRegionUnitWeakPointStat = new RSRegionUnitWeakPointStat();
            rsRegionUnitWeakPointStat.setAcode(1L);
            rsRegionUnitWeakPointStat.setAreaName("Test Area");
            rsRegionUnitWeakPointStat.setCcode(cityCode);
            rsRegionUnitWeakPointStat.setCityName("Test City");
            rsRegionUnitWeakPointStat.setCreateAt(new Date());

            List<RSLevelData> weakPoint = new ArrayList<>();
            RSLevelData rsLevelData = new RSLevelData();

            rsLevelData.setClassLevel(4);

            RSBookData rsBookData = new RSBookData();
            rsBookData.setBookId(2);
            rsBookData.setPress("Test");
            List<String> tags = new ArrayList<>();
            tags.add("1 tag");
            rsBookData.setTags(tags);
            List<RSBookData> rsBookDatas = new ArrayList<>();
            rsBookDatas.add(rsBookData);
            rsLevelData.setBooks(rsBookDatas);

            weakPoint.add(rsLevelData);

            rsRegionUnitWeakPointStat.setWeakPoint(weakPoint);

            rsRegionUnitWeakPointStatDao.save(rsRegionUnitWeakPointStat);

            if (i % 2 == 0) {
                rsRegionUnitWeakPointStatDao.save(rsRegionUnitWeakPointStat, 2014, Term.下学期);
            }
        }

        List<RSRegionUnitWeakPointStat> rsRegionUnitWeakPointStats = rsRegionUnitWeakPointStatDao.findByCityCode(cityCode);
        assertEquals(10, rsRegionUnitWeakPointStats.size());

        rsRegionUnitWeakPointStats = rsRegionUnitWeakPointStatDao.findByCityCode(cityCode, 2014, Term.下学期);
        assertEquals(5, rsRegionUnitWeakPointStats.size());
    }

    @Test
    public void testFindByCityCodes() throws Exception {
        List<Long> cityCodes = new ArrayList<>();
        cityCodes.add(110100L);
        cityCodes.add(110101L);

        for (Long cityCode : cityCodes) {
            for (long i = 1; i <= 5; i++) {
                RSRegionUnitWeakPointStat rsRegionUnitWeakPointStat = new RSRegionUnitWeakPointStat();
                rsRegionUnitWeakPointStat.setAcode(1L);
                rsRegionUnitWeakPointStat.setAreaName("Test Area");
                rsRegionUnitWeakPointStat.setCcode(cityCode);
                rsRegionUnitWeakPointStat.setCityName("Test City");
                rsRegionUnitWeakPointStat.setCreateAt(new Date());

                List<RSLevelData> weakPoint = new ArrayList<>();
                RSLevelData rsLevelData = new RSLevelData();

                rsLevelData.setClassLevel(4);

                RSBookData rsBookData = new RSBookData();
                rsBookData.setBookId(2);
                rsBookData.setPress("Test");
                List<String> tags = new ArrayList<>();
                tags.add("1 tag");
                rsBookData.setTags(tags);
                List<RSBookData> rsBookDatas = new ArrayList<>();
                rsBookDatas.add(rsBookData);
                rsLevelData.setBooks(rsBookDatas);

                weakPoint.add(rsLevelData);

                rsRegionUnitWeakPointStat.setWeakPoint(weakPoint);

                rsRegionUnitWeakPointStatDao.save(rsRegionUnitWeakPointStat);

                if (i % 2 == 0) {
                    rsRegionUnitWeakPointStatDao.save(rsRegionUnitWeakPointStat, 2014, Term.下学期);
                }
            }
        }

        List<RSRegionUnitWeakPointStat> rsRegionUnitWeakPointStats = rsRegionUnitWeakPointStatDao.findByCityCodes(cityCodes);
        assertEquals(10, rsRegionUnitWeakPointStats.size());

        rsRegionUnitWeakPointStats = rsRegionUnitWeakPointStatDao.findByCityCodes(cityCodes, 2014, Term.下学期);
        assertEquals(4, rsRegionUnitWeakPointStats.size());
    }
}
