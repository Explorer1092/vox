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

package com.voxlearning.utopia.service.campaign.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.mission.MissionIntegralLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = MissionIntegralLog.class)
public class TestMissionIntegralLogPersistence {

    @Inject private MissionIntegralLogPersistence missionIntegralLogPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = MissionIntegralLog.class)
    @MockBinder(
            type = MissionIntegralLog.class,
            jsons = {
                    "{'studentId':1,'missionId':0,'month':'2015-01'}",
                    "{'studentId':1,'missionId':0,'month':'2015-02'}",
                    "{'studentId':1,'missionId':0,'month':'2015-03'}",
            },
            persistence = MissionIntegralLogPersistence.class
    )
    public void testFindByStudentIdAndMonth() throws Exception {
        assertEquals(3, missionIntegralLogPersistence.findByStudentId(1L).size());
    }
}
