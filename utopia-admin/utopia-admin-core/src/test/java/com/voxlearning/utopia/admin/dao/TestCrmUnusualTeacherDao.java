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

package com.voxlearning.utopia.admin.dao;

import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.admin.entity.CrmUnusualTeacher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestCrmUnusualTeacherDao {

    @Inject private CrmUnusualTeacherDao crmUnusualTeacherDao;

    @Test
    @MockBinder(
            type = CrmUnusualTeacher.class,
            jsons = {
                    "{'cityCode':1}",
                    "{'cityCode':2}",
                    "{'cityCode':3}",
            },
            persistence = CrmUnusualTeacherDao.class
    )
    public void testFindCityUnusalTeachers() throws Exception {
        List<Integer> codes = Arrays.asList(1, 2, 3);
        List<CrmUnusualTeacher> list = crmUnusualTeacherDao.findCityUnusalTeachers(codes);
        assertEquals(3, list.size());
    }
}
