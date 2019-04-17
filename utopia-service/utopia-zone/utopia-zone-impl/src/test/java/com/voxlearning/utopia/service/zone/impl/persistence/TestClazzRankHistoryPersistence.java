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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.tempactivity.ClazzRankHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = ClazzRankHistory.class)
public class TestClazzRankHistoryPersistence {

    @Inject private ClazzRankHistoryPersistence clazzRankHistoryPersistence;

    @Test
    public void testClazzRankHistoryPersistence() throws Exception {
        ClazzRankHistory history = new ClazzRankHistory();
        history.setClazzId(100L);
        history.setRank(1);
        history.setMonth("201411");
        history.setLevel(1);
        history.setLevelScore(2);
        history.setSchoolName("");
        history.setClazzName("");
        clazzRankHistoryPersistence.insert(history);
        assertNotNull(clazzRankHistoryPersistence.load(history.getId()));

        List<ClazzRankHistory> list = clazzRankHistoryPersistence.findByClazzId(100L);
        assertEquals(1, list.size());
        list = clazzRankHistoryPersistence.findByMonth("201411");
        assertEquals(1, list.size());
    }
}

