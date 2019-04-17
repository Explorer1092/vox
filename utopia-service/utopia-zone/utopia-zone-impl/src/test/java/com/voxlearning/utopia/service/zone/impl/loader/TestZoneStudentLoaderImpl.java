/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.zone.impl.loader;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.context.MDP;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.impl.persistence.StudentInfoPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestZoneStudentLoaderImpl {

    @Inject private PersonalZoneLoaderImpl personalZoneLoader;

    @Test
    @TruncateDatabaseTable(databaseEntities = StudentInfo.class)
    @MockBinder(
            type = StudentInfo.class,
            jsons = {
                    "{'studentId':$nextLong}",
                    "{'studentId':$nextLong}",
                    "{'studentId':$nextLong}",
            },
            persistence = StudentInfoPersistence.class
    )
    public void testLoadStudentInfos() throws Exception {
        Set<Long> studentIds = MDP.toMap(StudentInfo.class, StudentInfo::getStudentId).keySet();
        Map<Long, StudentInfo> map = personalZoneLoader.loadStudentInfos(studentIds);
        assertEquals(studentIds.size(), map.size());
        for (Long studentId : studentIds) {
            assertNotNull(map.get(studentId));
            assertNotNull(personalZoneLoader.loadStudentInfo(studentId));
        }
    }
}
