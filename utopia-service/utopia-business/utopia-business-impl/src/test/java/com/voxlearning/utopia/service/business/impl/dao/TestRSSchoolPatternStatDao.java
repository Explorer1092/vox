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
import com.voxlearning.utopia.service.business.api.entity.RSSchoolPatternStat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2015/1/13.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestRSSchoolPatternStatDao {
    @Autowired private RSSchoolPatternStatDao rsSchoolPatternStatDao;

    @Test
    public void testFindByAreaCode() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolPatternStat rsSchoolPatternStat = new RSSchoolPatternStat();
            rsSchoolPatternStat.setSchoolId(1L);
            rsSchoolPatternStat.setSchoolName("Test School");
            rsSchoolPatternStat.setAcode(areaCode);
            rsSchoolPatternStat.setAreaName("Test Area");
            rsSchoolPatternStat.setCcode(1L);
            rsSchoolPatternStat.setCityName("Test City");
            rsSchoolPatternStat.setCreateAt(new Date());
            rsSchoolPatternStat.setPatterns(null);
            rsSchoolPatternStatDao.save(rsSchoolPatternStat);


        }

        List<RSSchoolPatternStat> rsSchoolPatternStats = rsSchoolPatternStatDao.findByAreaCode(areaCode);
        assertEquals(rsSchoolPatternStats.size(), 10);


    }

    @Test
    public void testFindByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolPatternStat rsSchoolPatternStat = new RSSchoolPatternStat();
                rsSchoolPatternStat.setSchoolId(1L);
                rsSchoolPatternStat.setSchoolName("Test School");
                rsSchoolPatternStat.setAcode(areaCode);
                rsSchoolPatternStat.setAreaName("Test Area");
                rsSchoolPatternStat.setCcode(1L);
                rsSchoolPatternStat.setCityName("Test City");
                rsSchoolPatternStat.setCreateAt(new Date());
                rsSchoolPatternStat.setPatterns(null);
                rsSchoolPatternStatDao.save(rsSchoolPatternStat);

                if (i % 2 == 0) {
                    rsSchoolPatternStatDao.save(rsSchoolPatternStat, 2014, Term.下学期);
                }
            }
        }

        List<RSSchoolPatternStat> rsSchoolPatternStats = rsSchoolPatternStatDao.findByAreaCodes(areaCodes);
        assertEquals(10, rsSchoolPatternStats.size());

        rsSchoolPatternStats = rsSchoolPatternStatDao.findByAreaCodes(areaCodes, 2014, Term.下学期);
        assertEquals(4, rsSchoolPatternStats.size());
    }

    @Test
    public void testCountByCityCode() throws Exception {
        long cityCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolPatternStat rsSchoolPatternStat = new RSSchoolPatternStat();
            rsSchoolPatternStat.setSchoolId(1L);
            rsSchoolPatternStat.setSchoolName("Test School");
            rsSchoolPatternStat.setAcode(1L);
            rsSchoolPatternStat.setAreaName("Test Area");
            rsSchoolPatternStat.setCcode(cityCode);
            rsSchoolPatternStat.setCityName("Test City");
            rsSchoolPatternStat.setCreateAt(new Date());
            rsSchoolPatternStat.setPatterns(null);
            rsSchoolPatternStat.setIsValid(true);
            rsSchoolPatternStatDao.save(rsSchoolPatternStat);

            if (i % 2 == 0) {
                rsSchoolPatternStatDao.save(rsSchoolPatternStat, 2014, Term.下学期);
            }
        }

        for (long i = 11; i <= 15; i++) {
            RSSchoolPatternStat rsSchoolPatternStat = new RSSchoolPatternStat();
            rsSchoolPatternStat.setSchoolId(1L);
            rsSchoolPatternStat.setSchoolName("Test School");
            rsSchoolPatternStat.setAcode(1L);
            rsSchoolPatternStat.setAreaName("Test Area");
            rsSchoolPatternStat.setCcode(cityCode);
            rsSchoolPatternStat.setCityName("Test City");
            rsSchoolPatternStat.setCreateAt(new Date());
            rsSchoolPatternStat.setPatterns(null);
            rsSchoolPatternStat.setIsValid(false);
            rsSchoolPatternStatDao.save(rsSchoolPatternStat);

            if (i % 2 == 0) {
                rsSchoolPatternStatDao.save(rsSchoolPatternStat, 2014, Term.下学期);
            }
        }

        long num = rsSchoolPatternStatDao.countByCityCode(cityCode, null, null);
        assertEquals(15, num);

        num = rsSchoolPatternStatDao.countByCityCode(cityCode, 2014, Term.下学期);
        assertEquals(7, num);

    }

    @Test
    public void testCountByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolPatternStat rsSchoolPatternStat = new RSSchoolPatternStat();
                rsSchoolPatternStat.setSchoolId(1L);
                rsSchoolPatternStat.setSchoolName("Test School");
                rsSchoolPatternStat.setAcode(areaCode);
                rsSchoolPatternStat.setAreaName("Test Area");
                rsSchoolPatternStat.setCcode(1L);
                rsSchoolPatternStat.setCityName("Test City");
                rsSchoolPatternStat.setCreateAt(new Date());
                rsSchoolPatternStat.setPatterns(null);
                rsSchoolPatternStatDao.save(rsSchoolPatternStat);

                if (i % 2 == 0) {
                    rsSchoolPatternStatDao.save(rsSchoolPatternStat, 2014, Term.下学期);
                }
            }
        }

        long num = rsSchoolPatternStatDao.countByAreaCodes(areaCodes, null, null);
        assertEquals(10, num);

        num = rsSchoolPatternStatDao.countByAreaCodes(areaCodes, 2014, Term.下学期);
        assertEquals(4, num);
    }
}
