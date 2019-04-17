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
import com.voxlearning.utopia.service.afenti.api.constant.AfentiLearningType;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserBookRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiLearningPlanUserBookRefPersistence {
    @Autowired private AfentiLearningPlanUserBookRefPersistence afentiLearningPlanUserBookRefPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserBookRef.class)
    public void testFindByUserIdAndSubject() throws Exception {
        AfentiLearningPlanUserBookRef ref = AfentiLearningPlanUserBookRef.newInstance(1L, true, "1", Subject.ENGLISH, AfentiLearningType.castle);
        afentiLearningPlanUserBookRefPersistence.persist(ref);
        assertEquals(1, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.ENGLISH).size());

        ref = AfentiLearningPlanUserBookRef.newInstance(1L, false, "2", Subject.ENGLISH, AfentiLearningType.castle);
        afentiLearningPlanUserBookRefPersistence.persist(ref);
        assertEquals(2, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.ENGLISH).size());

        ref = AfentiLearningPlanUserBookRef.newInstance(1L, true, "3", Subject.MATH, AfentiLearningType.castle);
        afentiLearningPlanUserBookRefPersistence.persist(ref);
        assertEquals(1, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.MATH).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserBookRef.class)
    public void testActivateAndInactivate() throws Exception {
        assertFalse(afentiLearningPlanUserBookRefPersistence.activate(1L, Subject.ENGLISH, "1", AfentiLearningType.castle));
        AfentiLearningPlanUserBookRef ref = AfentiLearningPlanUserBookRef.newInstance(1L, true, "1", Subject.ENGLISH, AfentiLearningType.castle);
        afentiLearningPlanUserBookRefPersistence.persist(ref);
        assertEquals(1, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.ENGLISH).size());

        assertTrue(afentiLearningPlanUserBookRefPersistence.inactivate(1L, Subject.ENGLISH, AfentiLearningType.castle));
        assertFalse(afentiLearningPlanUserBookRefPersistence.activate(1L, Subject.ENGLISH, "2", AfentiLearningType.castle));
        ref = AfentiLearningPlanUserBookRef.newInstance(1L, true, "2", Subject.ENGLISH, AfentiLearningType.castle);
        afentiLearningPlanUserBookRefPersistence.persist(ref);
        assertEquals(2, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.ENGLISH).size());

        assertTrue(afentiLearningPlanUserBookRefPersistence.inactivate(1L, Subject.ENGLISH, AfentiLearningType.castle));
        assertTrue(afentiLearningPlanUserBookRefPersistence.activate(1L, Subject.ENGLISH, "1", AfentiLearningType.castle));
        assertEquals(2, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.ENGLISH).size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserBookRef.class)
    public void testInactivateSpecificBook() throws Exception {
        assertFalse(afentiLearningPlanUserBookRefPersistence.activate(1L, Subject.ENGLISH, "1", AfentiLearningType.castle));
        AfentiLearningPlanUserBookRef ref = AfentiLearningPlanUserBookRef.newInstance(1L, true, "1", Subject.ENGLISH, AfentiLearningType.castle);
        afentiLearningPlanUserBookRefPersistence.persist(ref);
        assertEquals(1, afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(1L, Subject.ENGLISH).size());

        assertTrue(afentiLearningPlanUserBookRefPersistence.inactivate(1L, Subject.ENGLISH, "1", AfentiLearningType.castle));
    }
}
