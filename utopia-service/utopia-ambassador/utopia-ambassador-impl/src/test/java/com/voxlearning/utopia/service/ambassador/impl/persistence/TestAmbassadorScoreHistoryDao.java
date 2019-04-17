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

package com.voxlearning.utopia.service.ambassador.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorScoreHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AmbassadorScoreHistory.class)
public class TestAmbassadorScoreHistoryDao {

    @Inject
    private AmbassadorScoreHistoryDao ambassadorScoreHistoryDao;

    @Test
    public void testLoadAmbassadorTotalScore() throws Exception {
        AmbassadorScoreHistory history = new AmbassadorScoreHistory();
        history.setAmbassadorId(1L);
        history.setScore(1);
        ambassadorScoreHistoryDao.insert(history);
        history = new AmbassadorScoreHistory();
        history.setAmbassadorId(1L);
        history.setScore(2);
        ambassadorScoreHistoryDao.insert(history);
        history = new AmbassadorScoreHistory();
        history.setAmbassadorId(1L);
        history.setScore(3);
        ambassadorScoreHistoryDao.insert(history);
        assertEquals(6, ambassadorScoreHistoryDao.loadAmbassadorTotalScore(1L, new Date(0)).intValue());
    }

    @Test
    public void testLoadScoreHistory() throws Exception {
        for (int i = 0; i < 3; i++) {
            AmbassadorScoreHistory history = new AmbassadorScoreHistory();
            history.setAmbassadorId(1L);
            ambassadorScoreHistoryDao.insert(history);
            assertEquals(i + 1, ambassadorScoreHistoryDao.loadScoreHistory(1L, new Date(0)).size());
        }
    }

    @Test
    public void testDisableAmbassadorScore() throws Exception {
        AmbassadorScoreHistory history = new AmbassadorScoreHistory();
        history.setAmbassadorId(1L);
        ambassadorScoreHistoryDao.insert(history);
        Long id = history.getId();
        ambassadorScoreHistoryDao.disableAmbassadorScore(1L);
        assertTrue(ambassadorScoreHistoryDao.load(id).getDisabled());
    }
}
