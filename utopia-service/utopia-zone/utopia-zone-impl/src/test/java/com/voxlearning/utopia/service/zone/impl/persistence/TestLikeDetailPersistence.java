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

import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestLikeDetailPersistence {
    @Inject private LikeDetailPersistence likeDetailPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = LikeDetail.class)
    public void testFindByJournalIds() throws Exception {
        List<Long> journalIds = Arrays.asList(777L, 888L, 999L);
        for (Long journalId : journalIds) {
            for (long userId = 1; userId <= 3; userId++) {
                likeDetailPersistence.persist(LikeDetail.newInstance(journalId, 0L, 0L, userId));
            }
        }
        Map<Long, List<LikeDetail>> map = likeDetailPersistence.findByJournalIds(journalIds);
        assertEquals(journalIds.size(), map.size());
        for (Long journalId : journalIds) {
            assertEquals(3, map.get(journalId).size());
            assertEquals(3, likeDetailPersistence.findByJournalId(journalId).size());
        }
    }

    @Test
    @TruncateDatabaseTable(databaseEntities = LikeDetail.class)
    public void testFindByCreateDatetimeRange() throws Exception {
        for (int i = 0; i < 10; i++) {
            LikeDetail detail = LikeDetail.newInstance((long) i, 0L, 0L, 0L);
            detail.setCreateDatetime(new Date(20000));
            likeDetailPersistence.persist(detail);
        }
        Date start = new Date(10000);
        Date end = new Date(30000);

        Pageable pageable = new PageRequest(0, 4);
        Page<LikeDetail> page = likeDetailPersistence.findByCreateDatetimeRange(start, end, pageable);
        assertTrue(page.isFirst());
        assertFalse(page.isLast());
        assertFalse(page.hasPrevious());
        assertTrue(page.hasNext());
        assertEquals(10, page.getTotalElements());
        assertEquals(4, page.getNumberOfElements());
        assertEquals(3, page.getTotalPages());

        pageable = new PageRequest(1, 4);
        page = likeDetailPersistence.findByCreateDatetimeRange(start, end, pageable);
        assertFalse(page.isFirst());
        assertFalse(page.isLast());
        assertTrue(page.hasPrevious());
        assertTrue(page.hasNext());
        assertEquals(10, page.getTotalElements());
        assertEquals(4, page.getNumberOfElements());
        assertEquals(3, page.getTotalPages());

        pageable = new PageRequest(2, 4);
        page = likeDetailPersistence.findByCreateDatetimeRange(start, end, pageable);
        assertFalse(page.isFirst());
        assertTrue(page.isLast());
        assertTrue(page.hasPrevious());
        assertFalse(page.hasNext());
        assertEquals(10, page.getTotalElements());
        assertEquals(2, page.getNumberOfElements());
        assertEquals(3, page.getTotalPages());
    }
}
