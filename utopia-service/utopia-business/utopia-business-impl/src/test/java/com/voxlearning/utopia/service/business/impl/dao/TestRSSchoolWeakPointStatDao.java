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
import com.voxlearning.utopia.service.business.api.entity.RSSchoolWeakPointStat;
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
public class TestRSSchoolWeakPointStatDao {
    @Autowired private RSSchoolWeakPointStatDao rsSchoolWeakPointStatDao;

    @Test
    public void testFindByAreaCode() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolWeakPointStat rsSchoolWeakPointStat = new RSSchoolWeakPointStat();
            rsSchoolWeakPointStat.setSchoolId(1L);
            rsSchoolWeakPointStat.setSchoolName("Test School");
            rsSchoolWeakPointStat.setAcode(areaCode);
            rsSchoolWeakPointStat.setAreaName("Test Area");
            rsSchoolWeakPointStat.setCcode(1L);
            rsSchoolWeakPointStat.setCityName("Test City");
            rsSchoolWeakPointStat.setCreateAt(new Date());
            rsSchoolWeakPointStat.setTopic("topic");
            rsSchoolWeakPointStat.setWord("word");
            rsSchoolWeakPointStat.setGram("gram");
            rsSchoolWeakPointStatDao.save(rsSchoolWeakPointStat);
        }

        List<RSSchoolWeakPointStat> rsSchoolWeakPointStats = rsSchoolWeakPointStatDao.findByAreaCode(areaCode);
        assertEquals(rsSchoolWeakPointStats.size(), 10);
    }

    @Test
    public void testCountByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolWeakPointStat rsSchoolWeakPointStat = new RSSchoolWeakPointStat();
                rsSchoolWeakPointStat.setSchoolId(1L);
                rsSchoolWeakPointStat.setSchoolName("Test School");
                rsSchoolWeakPointStat.setAcode(areaCode);
                rsSchoolWeakPointStat.setAreaName("Test Area");
                rsSchoolWeakPointStat.setCcode(1L);
                rsSchoolWeakPointStat.setCityName("Test City");
                rsSchoolWeakPointStat.setCreateAt(new Date());
                rsSchoolWeakPointStat.setTopic("topic");
                rsSchoolWeakPointStat.setWord("word");
                rsSchoolWeakPointStat.setGram("gram");
                rsSchoolWeakPointStatDao.save(rsSchoolWeakPointStat);
            }
        }

        List<RSSchoolWeakPointStat> rsSchoolWeakPointStats = rsSchoolWeakPointStatDao.findByAreaCodes(areaCodes);
        assertEquals(rsSchoolWeakPointStats.size(), 10);
    }
}
