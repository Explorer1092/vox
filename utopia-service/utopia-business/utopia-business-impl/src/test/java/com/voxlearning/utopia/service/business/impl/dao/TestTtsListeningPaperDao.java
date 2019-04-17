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

import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestTtsListeningPaperDao {

    @Inject private TtsListeningPaperDao ttsListeningPaperDao;

    @Test
    @MockBinder(
            type = TtsListeningPaper.class,
            jsons = {
                    "{'author':1,'bookId':2,'title':'aaa'}",
                    "{'author':1,'bookId':2,'title':'AAA'}",
                    "{'author':1,'bookId':2,'title':'bbb'}",
                    "{'author':1,'bookId':2,'title':'BBB'}",
                    "{'author':1,'bookId':2,'title':'ccc'}",
                    "{'author':1,'bookId':2,'title':'CCC'}",
            },
            persistence = TtsListeningPaperDao.class
    )
    public void testGetListenPaperPageByUidAndBid() throws Exception {
        Pageable pageable = new PageRequest(0, 100);
        assertEquals(6, ttsListeningPaperDao.getListenPaperPageByUidAndBid(1L, 2L, pageable, null).getTotalElements());
        assertEquals(2, ttsListeningPaperDao.getListenPaperPageByUidAndBid(1L, 2L, pageable, "^a").getTotalElements());
        assertEquals(2, ttsListeningPaperDao.getListenPaperPageByUidAndBid(1L, 2L, pageable, "^b").getTotalElements());
        assertEquals(2, ttsListeningPaperDao.getListenPaperPageByUidAndBid(1L, 2L, pageable, "^c").getTotalElements());
    }
}
