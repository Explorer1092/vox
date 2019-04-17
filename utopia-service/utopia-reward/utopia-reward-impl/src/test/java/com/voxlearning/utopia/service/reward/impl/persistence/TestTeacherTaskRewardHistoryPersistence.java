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

package com.voxlearning.utopia.service.reward.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.ucenter.TeacherTaskRewardHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = TeacherTaskRewardHistory.class)
public class TestTeacherTaskRewardHistoryPersistence {

    @Inject private TeacherTaskRewardHistoryPersistence teacherTaskRewardHistoryPersistence;

    @Test
    public void testLoadByTeacherIdAndTaskType() throws Exception {
        long teacherId = 10000;
        String taskType = "T";
        for (int i = 0; i < 3; i++) {
            TeacherTaskRewardHistory history = new TeacherTaskRewardHistory();
            history.setTeacherId(teacherId);
            history.setTaskType(taskType);
            teacherTaskRewardHistoryPersistence.insert(history);
            assertEquals(i + 1, teacherTaskRewardHistoryPersistence.findByTeacherIdAndTaskType(teacherId, taskType).size());
        }
    }
}
