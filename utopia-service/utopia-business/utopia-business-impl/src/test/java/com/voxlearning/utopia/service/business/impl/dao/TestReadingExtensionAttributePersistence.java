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
import com.voxlearning.utopia.entity.content.ReadingExtensionAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by tanguohong on 14-8-6.
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestReadingExtensionAttributePersistence {

    @Inject private ReadingExtensionAttributePersistence readingExtensionAttributePersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = ReadingExtensionAttribute.class)
    public void findByBookIdAndUnitIdAndReadingIds() {
        List<Long> readingIds = Arrays.asList(301L, 302L, 303L);
        List<ReadingExtensionAttribute> attributes = new ArrayList<>();
        for (Long readingId : readingIds) {
            ReadingExtensionAttribute attribute = new ReadingExtensionAttribute();
            attribute.setBookId(101L);
            attribute.setUnitId(201L);
            attribute.setReadingId(readingId);
            attributes.add(attribute);
        }
        readingExtensionAttributePersistence.inserts(attributes);
        Map<Long, ReadingExtensionAttribute> map = readingExtensionAttributePersistence.findByBookIdAndUnitIdAndReadingIds(101L, 201L, readingIds);
        assertEquals(readingIds.size(), map.size());
        for (Long readingId : readingIds) {
            assertNotNull(map.get(readingId));
            assertNotNull(readingExtensionAttributePersistence.findByBookIdAndUnitIdAndReadingId(101L, 201L, readingId));
        }
    }
}
