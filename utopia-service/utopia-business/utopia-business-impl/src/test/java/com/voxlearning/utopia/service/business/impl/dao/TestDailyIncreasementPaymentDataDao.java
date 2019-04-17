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
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.business.api.entity.DailyIncreasementPaymentData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestDailyIncreasementPaymentDataDao {
    @Inject private DailyIncreasementPaymentDataDao dailyIncreasementPaymentDataDao;

    @Test
    @MockBinder(
            type = DailyIncreasementPaymentData.class,
            jsons = {
                    "{'region_code':1,'date':2,'status':1}",
                    "{'region_code':2,'date':2,'status':1}",
                    "{'region_code':3,'date':2,'status':1}",
            },
            persistence = DailyIncreasementPaymentDataDao.class
    )
    public void testDailyIncreasementPaymentDataDao() throws Exception {
        Set<Integer> set = Arrays.asList(1, 2, 3).stream().collect(Collectors.toSet());
        List<DailyIncreasementPaymentData> list = dailyIncreasementPaymentDataDao.findDailyIncreasementByRegionCode(1, 3, set);
        assertEquals(3, list.size());
        list = dailyIncreasementPaymentDataDao.findDailyIncreasementByRegionCodeSortByIncome(1, 3, set);
        assertEquals(3, list.size());
    }
}
