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

package com.voxlearning.utopia.service.business.impl.dao;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.entity.content.ReadingParts;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by tanguohong on 14-7-28.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestReadingPartsPersistence {
    @Inject private ReadingPartsPersistence readingPartsPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ReadingParts.class)
    public void findByReadingIds() {
        List<Long> readingIds = Arrays.asList(777L, 888L, 999L);
        List<ReadingParts> list = new ArrayList<>();
        for (Long readingId : readingIds) {
            for (int i = 0; i < 3; i++) {
                ReadingParts document = new ReadingParts();
                document.setReadingId(readingId);
                document.setPageNum(0);
                document.setEntext("");
                document.setCntext("");
                list.add(document);
            }
        }
        readingPartsPersistence.inserts(list);
        Map<Long, List<ReadingParts>> map = readingPartsPersistence.findByReadingIds(readingIds);
        assertEquals(readingIds.size(), map.size());
        for (Long readingId : readingIds) {
            assertEquals(3, map.get(readingId).size());
            assertEquals(3, readingPartsPersistence.findByReadingId(readingId).size());
        }
    }
}
