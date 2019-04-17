/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.business.api.entity.ReadingDraft;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestReadingDraftDao {
    @Inject private ReadingDraftDao readingDraftDao;

    @Test
    public void testVerifyDraft() throws Exception {
        String id1 = "foo";
        String id2 = RandomUtils.nextObjectId();

        ReadingDraft d1 = new ReadingDraft();
        d1.setId(id1);
        readingDraftDao.insert(d1);
        ReadingDraft d2 = new ReadingDraft();
        d2.setId(id2);
        readingDraftDao.insert(d2);

        readingDraftDao.verifyDraft(id1, "S1");
        readingDraftDao.verifyDraft(id2, "S2");

        assertEquals("S1", readingDraftDao.load(id1).getStatus());
        assertEquals("S2", readingDraftDao.load(id2).getStatus());
    }
}
