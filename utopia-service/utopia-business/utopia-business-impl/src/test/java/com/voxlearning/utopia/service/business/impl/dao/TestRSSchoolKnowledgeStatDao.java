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
import com.voxlearning.utopia.service.business.api.entity.RSSchoolKnowledgeStat;
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
public class TestRSSchoolKnowledgeStatDao {
    @Autowired private RSSchoolKnowledgeStatDao rsSchoolKnowledgeStatDao;

    @Test
    public void testFindByAreaCode() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
            rsSchoolKnowledgeStat.setSchoolId(1L);
            rsSchoolKnowledgeStat.setSchoolName("Test School");
            rsSchoolKnowledgeStat.setAcode(areaCode);
            rsSchoolKnowledgeStat.setAreaName("Test Area");
            rsSchoolKnowledgeStat.setCcode(1L);
            rsSchoolKnowledgeStat.setCityName("Test City");
            rsSchoolKnowledgeStat.setCreateAt(new Date());
            rsSchoolKnowledgeStat.setStuNum(100L);
            rsSchoolKnowledgeStat.setTopicNum(10);
            rsSchoolKnowledgeStat.setTopicRate(0.5);
            rsSchoolKnowledgeStat.setTopicPAvg(2);
            rsSchoolKnowledgeStat.setWordNum(10);
            rsSchoolKnowledgeStat.setWordRate(0.5);
            rsSchoolKnowledgeStat.setWordPAvg(2);
            rsSchoolKnowledgeStat.setGramNum(10);
            rsSchoolKnowledgeStat.setGramRate(0.5);
            rsSchoolKnowledgeStat.setGramPAvg(2);
            rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
        }

        List<RSSchoolKnowledgeStat> rsSchoolKnowledgeStats = rsSchoolKnowledgeStatDao.findByAreaCode(areaCode);
        assertEquals(rsSchoolKnowledgeStats.size(), 10);
    }

    @Test
    public void testFindByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
                rsSchoolKnowledgeStat.setSchoolId(1L);
                rsSchoolKnowledgeStat.setSchoolName("Test School");
                rsSchoolKnowledgeStat.setAcode(areaCode);
                rsSchoolKnowledgeStat.setAreaName("Test Area");
                rsSchoolKnowledgeStat.setCcode(1L);
                rsSchoolKnowledgeStat.setCityName("Test City");
                rsSchoolKnowledgeStat.setCreateAt(new Date());
                rsSchoolKnowledgeStat.setStuNum(100L);
                rsSchoolKnowledgeStat.setTopicNum(10);
                rsSchoolKnowledgeStat.setTopicRate(0.5);
                rsSchoolKnowledgeStat.setTopicPAvg(2);
                rsSchoolKnowledgeStat.setWordNum(10);
                rsSchoolKnowledgeStat.setWordRate(0.5);
                rsSchoolKnowledgeStat.setWordPAvg(2);
                rsSchoolKnowledgeStat.setGramNum(10);
                rsSchoolKnowledgeStat.setGramRate(0.5);
                rsSchoolKnowledgeStat.setGramPAvg(2);
                rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
            }
        }

        List<RSSchoolKnowledgeStat> rsSchoolKnowledgeStats = rsSchoolKnowledgeStatDao.findByAreaCodes(areaCodes);
        assertEquals(rsSchoolKnowledgeStats.size(), 10);
    }

    @Test
    public void testCountByAreaCodeAndIsValid() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
            rsSchoolKnowledgeStat.setSchoolId(1L);
            rsSchoolKnowledgeStat.setSchoolName("Test School");
            rsSchoolKnowledgeStat.setAcode(areaCode);
            rsSchoolKnowledgeStat.setAreaName("Test Area");
            rsSchoolKnowledgeStat.setCcode(1L);
            rsSchoolKnowledgeStat.setCityName("Test City");
            rsSchoolKnowledgeStat.setCreateAt(new Date());
            rsSchoolKnowledgeStat.setStuNum(100L);
            rsSchoolKnowledgeStat.setTopicNum(10);
            rsSchoolKnowledgeStat.setTopicRate(0.5);
            rsSchoolKnowledgeStat.setTopicPAvg(2);
            rsSchoolKnowledgeStat.setWordNum(10);
            rsSchoolKnowledgeStat.setWordRate(0.5);
            rsSchoolKnowledgeStat.setWordPAvg(2);
            rsSchoolKnowledgeStat.setGramNum(10);
            rsSchoolKnowledgeStat.setGramRate(0.5);
            rsSchoolKnowledgeStat.setGramPAvg(2);
            rsSchoolKnowledgeStat.setIsValid(true);
            rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
        }

        for (long i = 11; i <= 15; i++) {
            RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
            rsSchoolKnowledgeStat.setSchoolId(1L);
            rsSchoolKnowledgeStat.setSchoolName("Test School");
            rsSchoolKnowledgeStat.setAcode(areaCode);
            rsSchoolKnowledgeStat.setAreaName("Test Area");
            rsSchoolKnowledgeStat.setCcode(1L);
            rsSchoolKnowledgeStat.setCityName("Test City");
            rsSchoolKnowledgeStat.setCreateAt(new Date());
            rsSchoolKnowledgeStat.setStuNum(100L);
            rsSchoolKnowledgeStat.setTopicNum(10);
            rsSchoolKnowledgeStat.setTopicRate(0.5);
            rsSchoolKnowledgeStat.setTopicPAvg(2);
            rsSchoolKnowledgeStat.setWordNum(10);
            rsSchoolKnowledgeStat.setWordRate(0.5);
            rsSchoolKnowledgeStat.setWordPAvg(2);
            rsSchoolKnowledgeStat.setGramNum(10);
            rsSchoolKnowledgeStat.setGramRate(0.5);
            rsSchoolKnowledgeStat.setGramPAvg(2);
            rsSchoolKnowledgeStat.setIsValid(false);
            rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
        }

        long validNum = rsSchoolKnowledgeStatDao.countByAreaCodeAndIsValid(areaCode, true, null, null);
        long invalidNum = rsSchoolKnowledgeStatDao.countByAreaCodeAndIsValid(areaCode, false, null, null);

        assertEquals(validNum, 10);
        assertEquals(invalidNum, 5);
    }

    @Test
    public void testCountByCityCode() throws Exception {
        long cityCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
            rsSchoolKnowledgeStat.setSchoolId(1L);
            rsSchoolKnowledgeStat.setSchoolName("Test School");
            rsSchoolKnowledgeStat.setAcode(2L);
            rsSchoolKnowledgeStat.setAreaName("Test Area");
            rsSchoolKnowledgeStat.setCcode(cityCode);
            rsSchoolKnowledgeStat.setCityName("Test City");
            rsSchoolKnowledgeStat.setCreateAt(new Date());
            rsSchoolKnowledgeStat.setStuNum(100L);
            rsSchoolKnowledgeStat.setTopicNum(10);
            rsSchoolKnowledgeStat.setTopicRate(0.5);
            rsSchoolKnowledgeStat.setTopicPAvg(2);
            rsSchoolKnowledgeStat.setWordNum(10);
            rsSchoolKnowledgeStat.setWordRate(0.5);
            rsSchoolKnowledgeStat.setWordPAvg(2);
            rsSchoolKnowledgeStat.setGramNum(10);
            rsSchoolKnowledgeStat.setGramRate(0.5);
            rsSchoolKnowledgeStat.setGramPAvg(2);
            rsSchoolKnowledgeStat.setIsValid(false);
            rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
        }

        for (long i = 11; i <= 15; i++) {
            RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
            rsSchoolKnowledgeStat.setSchoolId(1L);
            rsSchoolKnowledgeStat.setSchoolName("Test School");
            rsSchoolKnowledgeStat.setAcode(2L);
            rsSchoolKnowledgeStat.setAreaName("Test Area");
            rsSchoolKnowledgeStat.setCcode(cityCode);
            rsSchoolKnowledgeStat.setCityName("Test City");
            rsSchoolKnowledgeStat.setCreateAt(new Date());
            rsSchoolKnowledgeStat.setStuNum(100L);
            rsSchoolKnowledgeStat.setTopicNum(10);
            rsSchoolKnowledgeStat.setTopicRate(0.5);
            rsSchoolKnowledgeStat.setTopicPAvg(2);
            rsSchoolKnowledgeStat.setWordNum(10);
            rsSchoolKnowledgeStat.setWordRate(0.5);
            rsSchoolKnowledgeStat.setWordPAvg(2);
            rsSchoolKnowledgeStat.setGramNum(10);
            rsSchoolKnowledgeStat.setGramRate(0.5);
            rsSchoolKnowledgeStat.setGramPAvg(2);
            rsSchoolKnowledgeStat.setIsValid(true);
            rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
        }

        long num = rsSchoolKnowledgeStatDao.countByCityCode(cityCode, null, null);

        assertEquals(num, 15);
    }

    @Test
    public void testCountByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolKnowledgeStat rsSchoolKnowledgeStat = new RSSchoolKnowledgeStat();
                rsSchoolKnowledgeStat.setSchoolId(1L);
                rsSchoolKnowledgeStat.setSchoolName("Test School");
                rsSchoolKnowledgeStat.setAcode(areaCode);
                rsSchoolKnowledgeStat.setAreaName("Test Area");
                rsSchoolKnowledgeStat.setCcode(1L);
                rsSchoolKnowledgeStat.setCityName("Test City");
                rsSchoolKnowledgeStat.setCreateAt(new Date());
                rsSchoolKnowledgeStat.setStuNum(100L);
                rsSchoolKnowledgeStat.setTopicNum(10);
                rsSchoolKnowledgeStat.setTopicRate(0.5);
                rsSchoolKnowledgeStat.setTopicPAvg(2);
                rsSchoolKnowledgeStat.setWordNum(10);
                rsSchoolKnowledgeStat.setWordRate(0.5);
                rsSchoolKnowledgeStat.setWordPAvg(2);
                rsSchoolKnowledgeStat.setGramNum(10);
                rsSchoolKnowledgeStat.setGramRate(0.5);
                rsSchoolKnowledgeStat.setGramPAvg(2);
                rsSchoolKnowledgeStatDao.save(rsSchoolKnowledgeStat);
            }
        }

        long num = rsSchoolKnowledgeStatDao.countByAreaCodes(areaCodes, null, null);

        assertEquals(num, 10);
    }
}
