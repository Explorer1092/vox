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
import com.voxlearning.utopia.service.business.api.entity.RSRegionKnowledgeStat;
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
public class TestRSRegionKnowledgeStatDao {
    @Autowired private RSRegionKnowledgeStatDao rsRegionKnowledgeStatDao;

    @Test
    public void testFindByCityCode() throws Exception {
        long cityCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSRegionKnowledgeStat rsRegionKnowledgeStat = new RSRegionKnowledgeStat();
            rsRegionKnowledgeStat.setAcode(1L);
            rsRegionKnowledgeStat.setAreaName("Test Area");
            rsRegionKnowledgeStat.setCcode(cityCode);
            rsRegionKnowledgeStat.setCityName("Test City");
            rsRegionKnowledgeStat.setCreateAt(new Date());
            rsRegionKnowledgeStat.setStuNum(100L);
            rsRegionKnowledgeStat.setTopicNum(10);
            rsRegionKnowledgeStat.setTopicRate(0.5);
            rsRegionKnowledgeStat.setTopicPAvg(2);
            rsRegionKnowledgeStat.setWordNum(10);
            rsRegionKnowledgeStat.setWordRate(0.5);
            rsRegionKnowledgeStat.setWordPAvg(2);
            rsRegionKnowledgeStat.setGramNum(10);
            rsRegionKnowledgeStat.setGramRate(0.5);
            rsRegionKnowledgeStat.setGramPAvg(2);
            rsRegionKnowledgeStatDao.save(rsRegionKnowledgeStat);

            if (i % 2 == 0) {
                rsRegionKnowledgeStatDao.save(rsRegionKnowledgeStat, 2014, Term.下学期);
            }
        }

        List<RSRegionKnowledgeStat> rsRegionKnowledgeStats = rsRegionKnowledgeStatDao.findByCityCode(cityCode);
        assertEquals(10, rsRegionKnowledgeStats.size());

        rsRegionKnowledgeStats = rsRegionKnowledgeStatDao.findByCityCode(cityCode, 2014, Term.下学期);
        assertEquals(5, rsRegionKnowledgeStats.size());
    }

    @Test
    public void testFindByCityCodes() throws Exception {
        List<Long> cityCodes = new ArrayList<>();
        cityCodes.add(110100L);
        cityCodes.add(110101L);
        for (Long cityCode : cityCodes) {
            for (long i = 1; i <= 5; i++) {
                RSRegionKnowledgeStat rsRegionKnowledgeStat = new RSRegionKnowledgeStat();
                rsRegionKnowledgeStat.setAcode(1L);
                rsRegionKnowledgeStat.setAreaName("Test Area");
                rsRegionKnowledgeStat.setCcode(cityCode);
                rsRegionKnowledgeStat.setCityName("Test City");
                rsRegionKnowledgeStat.setCreateAt(new Date());
                rsRegionKnowledgeStat.setStuNum(100L);
                rsRegionKnowledgeStat.setTopicNum(10);
                rsRegionKnowledgeStat.setTopicRate(0.5);
                rsRegionKnowledgeStat.setTopicPAvg(2);
                rsRegionKnowledgeStat.setWordNum(10);
                rsRegionKnowledgeStat.setWordRate(0.5);
                rsRegionKnowledgeStat.setWordPAvg(2);
                rsRegionKnowledgeStat.setGramNum(10);
                rsRegionKnowledgeStat.setGramRate(0.5);
                rsRegionKnowledgeStat.setGramPAvg(2);
                rsRegionKnowledgeStatDao.save(rsRegionKnowledgeStat);

                if (i % 2 == 0) {
                    rsRegionKnowledgeStatDao.save(rsRegionKnowledgeStat, 2014, Term.下学期);
                }
            }
        }

        List<RSRegionKnowledgeStat> rsRegionKnowledgeStats = rsRegionKnowledgeStatDao.findByCityCodes(cityCodes);
        assertEquals(10, rsRegionKnowledgeStats.size());

        rsRegionKnowledgeStats = rsRegionKnowledgeStatDao.findByCityCodes(cityCodes, 2014, Term.下学期);
        assertEquals(4, rsRegionKnowledgeStats.size());
    }
}
