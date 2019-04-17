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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.cache.ValueWrapper;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserFootprint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Ruib
 * @since 2016/7/14
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAfentiLearningPlanUserFootprintPersistence {
    @Inject AfentiLearningPlanUserFootprintPersistence p;

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserFootprint.class)
    public void testFindByUserIdAndSubject() throws Exception {
        AfentiLearningPlanUserFootprint footprint = p.findByUserIdAndSubject(30013L, Subject.ENGLISH);
        assertNull(footprint);

        footprint = new AfentiLearningPlanUserFootprint();
        footprint.setNewBookId("BK-103000000");
        footprint.setNewUnitId("BKN-103111111");
        footprint.setUserId(30013L);
        footprint.setSubject(Subject.ENGLISH);
        footprint.setRank(0);
        p.insert(footprint);

        footprint = p.findByUserIdAndSubject(30013L, Subject.ENGLISH);
        assertNotNull(footprint);
        assertEquals("BK-103000000", footprint.getNewBookId());
        assertEquals("BKN-103111111", footprint.getNewUnitId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserFootprint.class)
    public void testFindByUserIdsAndSubject() throws Exception {
        AfentiLearningPlanUserFootprint footprint = new AfentiLearningPlanUserFootprint();
        footprint.setNewBookId("BK-103000000");
        footprint.setNewUnitId("BKN-103111111");
        footprint.setUserId(30013L);
        footprint.setSubject(Subject.ENGLISH);
        footprint.setRank(0);
        p.insert(footprint);

        footprint = new AfentiLearningPlanUserFootprint();
        footprint.setNewBookId("BK-103000000");
        footprint.setNewUnitId("BKN-103111111");
        footprint.setUserId(30011L);
        footprint.setSubject(Subject.ENGLISH);
        footprint.setRank(0);
        p.insert(footprint);

        footprint = new AfentiLearningPlanUserFootprint();
        footprint.setNewBookId("BK-103000000");
        footprint.setNewUnitId("BKN-103111111");
        footprint.setUserId(30012L);
        footprint.setSubject(Subject.MATH);
        footprint.setRank(0);
        p.insert(footprint);

        List<Long> userIds = Arrays.asList(30011L, 30012L, 30013L, 30014L);

        Map<Long, AfentiLearningPlanUserFootprint> map = p.findByUserIdsAndSubject(userIds, Subject.ENGLISH);
        assertEquals(2, map.size());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserFootprint.class)
    public void testUpdate() throws Exception {
        AfentiLearningPlanUserFootprint footprint = new AfentiLearningPlanUserFootprint();
        footprint.setNewBookId("BK-103000000");
        footprint.setNewUnitId("BKN-103111111");
        footprint.setUserId(30013L);
        footprint.setSubject(Subject.ENGLISH);
        footprint.setRank(0);
        p.insert(footprint);

        AfentiLearningPlanUserFootprint entity = p.findByUserIdAndSubject(30013L, Subject.ENGLISH);
        assertNotNull(entity);
        assertEquals("BK-103000000", entity.getNewBookId());
        assertEquals("BKN-103111111", entity.getNewUnitId());

        entity.setNewBookId("BK-103000001");
        entity.setNewUnitId("BKN-103111112");
        p.update(entity);

        AfentiLearningPlanUserFootprint another = p.findByUserIdAndSubject(30013L, Subject.ENGLISH);
        assertNotNull(another);
        assertEquals("BK-103000001", another.getNewBookId());
        assertEquals("BKN-103111112", another.getNewUnitId());
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = AfentiLearningPlanUserFootprint.class)
    public void testUpdateWrapper() throws Exception {
        AfentiLearningPlanUserFootprint footprint = new AfentiLearningPlanUserFootprint();
        footprint.setNewBookId("BK-103000000");
        footprint.setNewUnitId("BKN-103111111");
        footprint.setUserId(30013L);
        footprint.setSubject(Subject.ENGLISH);
        footprint.setRank(0);
        p.insert(footprint);

        // force set null wrapper into cache
        String key = CacheKeyGenerator.generateCacheKey(AfentiLearningPlanUserFootprint.class,
                new String[]{"UID", "S"}, new Object[]{footprint.getUserId(), footprint.getSubject()});
        p.getCache().set(key, 3600, new ValueWrapper.SerializableValueWrapper(null));

        // the update will evict the cache for UOE
        footprint.setRank(1);
        assertTrue(p.update(footprint));

        // will read from database
        footprint = p.findByUserIdAndSubject(30013L, Subject.ENGLISH);
        assertEquals(1, footprint.getRank().intValue());
    }
}
