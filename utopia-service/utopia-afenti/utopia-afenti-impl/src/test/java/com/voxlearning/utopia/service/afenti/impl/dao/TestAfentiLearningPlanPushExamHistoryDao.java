/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.spi.test.RebuildDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanPushExamHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiLearningPlanPushExamHistoryDao {

    @Inject private AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_LEARNING_PLAN_PUSH_EXAMINATION_HISTORY_{}", arguments = {"9"})
    public void testQueryByUserId() throws Exception {
        long userId = 30009;
        for (int i = 0; i < 3; i++) {
            AfentiLearningPlanPushExamHistory document = new AfentiLearningPlanPushExamHistory();
            document.setUserId(userId);
            afentiLearningPlanPushExamHistoryDao.insert(document);
            assertEquals(i + 1, afentiLearningPlanPushExamHistoryDao.$queryByUserId(userId).size());
        }
    }

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_LEARNING_PLAN_PUSH_EXAMINATION_HISTORY_{}", arguments = {"33"})
    public void testUpdateRightAndErrorNums() throws Exception {
        AfentiLearningPlanPushExamHistory document = new AfentiLearningPlanPushExamHistory();
        document.setUserId(30033L);
        afentiLearningPlanPushExamHistoryDao.insert(document);

        afentiLearningPlanPushExamHistoryDao.queryByUserIdAndNewBookId(30033L, "");

        document.setRightNum(1);
        document.setErrorNum(3);
        afentiLearningPlanPushExamHistoryDao.updateRightAndErrorNums(document);

        document = afentiLearningPlanPushExamHistoryDao.queryByUserIdAndNewBookId(30033L, "")
                .stream().findFirst().orElse(null);
        assertEquals(1, document.getRightNum().intValue());
        assertEquals(3, document.getErrorNum().intValue());
    }

    @Test
    @RebuildDatabaseTable(tableTemplate = "VOX_AFENTI_LEARNING_PLAN_PUSH_EXAMINATION_HISTORY_{}", arguments = {"46"})
    public void testDelete() throws Exception {
        AfentiLearningPlanPushExamHistory document = new AfentiLearningPlanPushExamHistory();
        document.setUserId(30046L);
        document.setRank(1);
        afentiLearningPlanPushExamHistoryDao.insert(document);

        afentiLearningPlanPushExamHistoryDao.delete(30046L, "", "", 1);
    }
}
