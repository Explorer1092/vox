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

package com.voxlearning.utopia.service.business.impl.persistence;

import com.voxlearning.alps.annotation.cache.IgnoreFlushCache;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.BizStudentVoice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@IgnoreFlushCache
@DropMongoDatabase
public class TestBizStudentVoicePersistence {
    @Inject private BizStudentVoicePersistence bizStudentVoicePersistence;

    @Test
    @MockBinder(
            type = BizStudentVoice.class,
            jsons = {
                    "{'clazzId':1}",
                    "{'clazzId':1}",
                    "{'clazzId':1}",
                    "{'clazzId':2}",
                    "{'clazzId':2}",
                    "{'clazzId':2}",
                    "{'clazzId':3}",
                    "{'clazzId':3}",
                    "{'clazzId':3}",
            },
            persistence = BizStudentVoicePersistence.class
    )
    public void testFindByClazzIds() throws Exception {
        List<Long> clazzIds = Arrays.asList(1L, 2L, 3L);
        List<BizStudentVoice> list = bizStudentVoicePersistence.findByClazzIds(clazzIds, 400);
        assertEquals(9, list.size());
    }
}
