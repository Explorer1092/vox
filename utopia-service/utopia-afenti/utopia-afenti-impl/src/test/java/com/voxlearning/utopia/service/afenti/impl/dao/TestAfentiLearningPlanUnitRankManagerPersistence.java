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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiLearningPlanUnitRankManagerPersistence {
    @Autowired private AfentiLearningPlanUnitRankManagerPersistence afentiLearningPlanUnitRankManagerPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUnitRankManager.class)
    public void testFindByBookId() throws Exception {
        assertEquals(0, afentiLearningPlanUnitRankManagerPersistence.findByNewBookId("1").size());

        AfentiLearningPlanUnitRankManager manager = newInstance();
        manager.setNewBookId("1");
        afentiLearningPlanUnitRankManagerPersistence.persist(manager);
        assertEquals(1, afentiLearningPlanUnitRankManagerPersistence.findByNewBookId("1").size());

        manager = newInstance();
        manager.setNewBookId("1");
        afentiLearningPlanUnitRankManagerPersistence.persist(manager);
        assertEquals(2, afentiLearningPlanUnitRankManagerPersistence.findByNewBookId("1").size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUnitRankManager.class)
    public void testFindAllNewBookIds() throws Exception {
        AfentiLearningPlanUnitRankManager manager = newInstance();
        for (int i = 0; i < 10; i++) {
            manager.setNewBookId(String.valueOf(i));
            afentiLearningPlanUnitRankManagerPersistence.persist(manager);
        }
        List<String> all = afentiLearningPlanUnitRankManagerPersistence.findAllBookIds();
        assertEquals(10, all.size());
        afentiLearningPlanUnitRankManagerPersistence.findAllBookIds();
    }

    public AfentiLearningPlanUnitRankManager newInstance() {
        AfentiLearningPlanUnitRankManager manager = new AfentiLearningPlanUnitRankManager();
        manager.setNewBookId("");
        manager.setNewUnitId("");
        manager.setUnitRank(0);
        manager.setUnitName("");
        manager.setRank(0);
        manager.setRankType("");
        manager.setSubject(Subject.ENGLISH);
        return manager;
    }
}
