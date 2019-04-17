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
import com.voxlearning.utopia.service.business.api.entity.RSSchoolUnitWeakPointStat;
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
public class TestRSSchoolUnitWeakPointStatDao {
    @Autowired private RSSchoolUnitWeakPointStatDao rsSchoolUnitWeakPointStatDao;

    @Test
    public void testFindByAreaCode() throws Exception {
        long areaCode = 110100;
        for (long i = 1; i <= 10; i++) {
            RSSchoolUnitWeakPointStat rsSchoolUnitWeakPointStat = new RSSchoolUnitWeakPointStat();
            rsSchoolUnitWeakPointStat.setSchoolName("Test School");
            rsSchoolUnitWeakPointStat.setSchoolId(1L);
            rsSchoolUnitWeakPointStat.setAcode(areaCode);
            rsSchoolUnitWeakPointStat.setAreaName("Test Area");
            rsSchoolUnitWeakPointStat.setCcode(1L);
            rsSchoolUnitWeakPointStat.setCityName("Test City");
            rsSchoolUnitWeakPointStat.setCreateAt(new Date());

            List<RSLevelData> weakPoint = new ArrayList<>();
            RSLevelData rsLevelData = new RSLevelData();
            rsLevelData.setClassLevel(4);

            RSBookData rsBookData = new RSBookData();
            rsBookData.setBookId(2);
            rsBookData.setPress("Test");
            List<String> tags = new ArrayList<>();
            tags.add("1 tag");
            rsBookData.setTags(tags);
            List<RSBookData> rsBookDatas = new ArrayList<RSBookData>();
            rsBookDatas.add(rsBookData);
            rsLevelData.setBooks(rsBookDatas);

            weakPoint.add(rsLevelData);

            rsSchoolUnitWeakPointStat.setWeakPoint(weakPoint);

            rsSchoolUnitWeakPointStatDao.save(rsSchoolUnitWeakPointStat);
        }

        List<RSSchoolUnitWeakPointStat> rsSchoolUnitWeakPointStats = rsSchoolUnitWeakPointStatDao.findByAreaCode(areaCode);
        assertEquals(rsSchoolUnitWeakPointStats.size(), 10);
    }

    @Test
    public void testFindByAreaCodes() throws Exception {
        List<Long> areaCodes = new ArrayList<>();
        areaCodes.add(110100L);
        areaCodes.add(110101L);
        for (Long areaCode : areaCodes) {
            for (long i = 1; i <= 5; i++) {
                RSSchoolUnitWeakPointStat rsSchoolUnitWeakPointStat = new RSSchoolUnitWeakPointStat();
                rsSchoolUnitWeakPointStat.setSchoolName("Test School");
                rsSchoolUnitWeakPointStat.setSchoolId(1L);
                rsSchoolUnitWeakPointStat.setAcode(areaCode);
                rsSchoolUnitWeakPointStat.setAreaName("Test Area");
                rsSchoolUnitWeakPointStat.setCcode(1L);
                rsSchoolUnitWeakPointStat.setCityName("Test City");
                rsSchoolUnitWeakPointStat.setCreateAt(new Date());

                List<RSLevelData> weakPoint = new ArrayList<>();
                RSLevelData rsLevelData = new RSLevelData();
                rsLevelData.setClassLevel(4);

                RSBookData rsBookData = new RSBookData();
                rsBookData.setBookId(2);
                rsBookData.setPress("Test");
                List<String> tags = new ArrayList<>();
                tags.add("1 tag");
                rsBookData.setTags(tags);
                List<RSBookData> rsBookDatas = new ArrayList<RSBookData>();
                rsBookDatas.add(rsBookData);
                rsLevelData.setBooks(rsBookDatas);

                weakPoint.add(rsLevelData);

                rsSchoolUnitWeakPointStat.setWeakPoint(weakPoint);

                rsSchoolUnitWeakPointStatDao.save(rsSchoolUnitWeakPointStat);
            }
        }
        List<RSSchoolUnitWeakPointStat> rsSchoolUnitWeakPointStats = rsSchoolUnitWeakPointStatDao.findByAreaCodes(areaCodes);
        assertEquals(rsSchoolUnitWeakPointStats.size(), 10);
    }
}
