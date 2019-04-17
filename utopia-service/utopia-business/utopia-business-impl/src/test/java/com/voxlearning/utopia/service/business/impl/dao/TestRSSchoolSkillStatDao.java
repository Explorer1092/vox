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

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.business.api.entity.RSSchoolSkillStat;
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
public class TestRSSchoolSkillStatDao {
    @Autowired private RSSchoolSkillStatDao rsSchoolSkillStatDao;

    @Test
    public void testFindByAreaCode() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
            rsSchoolSkillStat.setSchoolId(1L);
            rsSchoolSkillStat.setSchoolName("Test School");
            rsSchoolSkillStat.setAcode(areaCode);
            rsSchoolSkillStat.setAreaName("Test Area");
            rsSchoolSkillStat.setCcode(1L);
            rsSchoolSkillStat.setCityName("Test City");
            rsSchoolSkillStat.setCreateAt(new Date());
            rsSchoolSkillStat.setSkills(null);
            rsSchoolSkillStat.setStuNum(100L);
            rsSchoolSkillStatDao.save(rsSchoolSkillStat);
        }

        List<RSSchoolSkillStat> rsSchoolSkillStats = rsSchoolSkillStatDao.findByAreaCode(areaCode);
        assertEquals(rsSchoolSkillStats.size(), 10);
    }

    @Test
    public void testFindByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
                rsSchoolSkillStat.setSchoolId(1L);
                rsSchoolSkillStat.setSchoolName("Test School");
                rsSchoolSkillStat.setAcode(areaCode);
                rsSchoolSkillStat.setAreaName("Test Area");
                rsSchoolSkillStat.setCcode(1L);
                rsSchoolSkillStat.setCityName("Test City");
                rsSchoolSkillStat.setCreateAt(new Date());
                rsSchoolSkillStat.setSkills(null);
                rsSchoolSkillStat.setStuNum(100L);
                rsSchoolSkillStatDao.save(rsSchoolSkillStat);
            }
        }

        List<RSSchoolSkillStat> rsSchoolSkillStats = rsSchoolSkillStatDao.findByAreaCodes(areaCodes);
        assertEquals(rsSchoolSkillStats.size(), 10);
    }

    @Test
    public void testCountByAreaCodeAndIsValid() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
            rsSchoolSkillStat.setSchoolId(1L);
            rsSchoolSkillStat.setSchoolName("Test School");
            rsSchoolSkillStat.setAcode(areaCode);
            rsSchoolSkillStat.setAreaName("Test Area");
            rsSchoolSkillStat.setCcode(1L);
            rsSchoolSkillStat.setCityName("Test City");
            rsSchoolSkillStat.setCreateAt(new Date());
            rsSchoolSkillStat.setSkills(null);
            rsSchoolSkillStat.setStuNum(100L);
            rsSchoolSkillStat.setIsValid(true);
            rsSchoolSkillStatDao.save(rsSchoolSkillStat);
        }

        for (long i = 11; i <= 15; i++) {
            RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
            rsSchoolSkillStat.setSchoolId(1L);
            rsSchoolSkillStat.setSchoolName("Test School");
            rsSchoolSkillStat.setAcode(areaCode);
            rsSchoolSkillStat.setAreaName("Test Area");
            rsSchoolSkillStat.setCcode(1L);
            rsSchoolSkillStat.setCityName("Test City");
            rsSchoolSkillStat.setCreateAt(new Date());
            rsSchoolSkillStat.setSkills(null);
            rsSchoolSkillStat.setStuNum(100L);
            rsSchoolSkillStat.setIsValid(false);
            rsSchoolSkillStatDao.save(rsSchoolSkillStat);
        }

        long validNum = rsSchoolSkillStatDao.countByAreaCodeAndIsValid(areaCode, true, null, null);
        long invalidNum = rsSchoolSkillStatDao.countByAreaCodeAndIsValid(areaCode, false, null, null);

        assertEquals(validNum, 10);
        assertEquals(invalidNum, 5);
    }

    @Test
    public void testCountByCityCode() throws Exception {
        long cityCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
            rsSchoolSkillStat.setSchoolId(1L);
            rsSchoolSkillStat.setSchoolName("Test School");
            rsSchoolSkillStat.setAcode(1L);
            rsSchoolSkillStat.setAreaName("Test Area");
            rsSchoolSkillStat.setCcode(cityCode);
            rsSchoolSkillStat.setCityName("Test City");
            rsSchoolSkillStat.setCreateAt(new Date());
            rsSchoolSkillStat.setSkills(null);
            rsSchoolSkillStat.setIsValid(true);
            rsSchoolSkillStatDao.save(rsSchoolSkillStat);
        }

        for (long i = 11; i <= 15; i++) {
            RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
            rsSchoolSkillStat.setSchoolId(1L);
            rsSchoolSkillStat.setSchoolName("Test School");
            rsSchoolSkillStat.setAcode(1L);
            rsSchoolSkillStat.setAreaName("Test Area");
            rsSchoolSkillStat.setCcode(cityCode);
            rsSchoolSkillStat.setCityName("Test City");
            rsSchoolSkillStat.setCreateAt(new Date());
            rsSchoolSkillStat.setSkills(null);
            rsSchoolSkillStat.setIsValid(true);
            rsSchoolSkillStatDao.save(rsSchoolSkillStat);
        }

        long num = rsSchoolSkillStatDao.countByCityCode(cityCode, null, null);

        assertEquals(num, 15);
    }

    @Test
    public void testCountByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolSkillStat rsSchoolSkillStat = new RSSchoolSkillStat();
                rsSchoolSkillStat.setSchoolId(1L);
                rsSchoolSkillStat.setSchoolName("Test School");
                rsSchoolSkillStat.setAcode(areaCode);
                rsSchoolSkillStat.setAreaName("Test Area");
                rsSchoolSkillStat.setCcode(1L);
                rsSchoolSkillStat.setCityName("Test City");
                rsSchoolSkillStat.setCreateAt(new Date());
                rsSchoolSkillStat.setSkills(null);
                rsSchoolSkillStatDao.save(rsSchoolSkillStat);
            }
        }

        long num = rsSchoolSkillStatDao.countByAreaCodes(areaCodes, null, null);

        assertEquals(num, 10);
    }
}
