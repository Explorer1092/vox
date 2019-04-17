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

package com.voxlearning.utopia.service.psr.impl;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.psr.consumer.UtopiaPsrServiceClient;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestUtopiaPsrServiceImpl extends SpringContainerSupport {
    @Inject private UtopiaPsrServiceClient utopiaPsrServiceClient;

    /*
        String product, String uType,
        Long userId, int regionCode, int bookId, int unitId, int eCount,
        float minP, float maxP)
      */
    @Test
    public void testGetPsrExam() throws Exception {

//        TestUser testUser = new TestUser();
//        testUser.setEkCouchbaseDao(ekCouchbaseDao);
//        testUser.deal();

//        testExamEnSimilar();
//        testAppEn();
//        testExamEn();
//        testOther();

    }

    public void testExamEn() {
        String product = "17zuoye";
        String uType = "student";
        long userId = 339330887L; //333742818L;
        int regionCode = 410300;
        long bookId = 580L;
        long unitId = 581L;
        int eCount = 5;
        float minP = (float) 0.7;
        float maxP = (float) 0.65;

        product = "BABEL";

        regionCode = 110100;
        userId = 348928957L;
        bookId = 1197L;
        unitId = 4966L;
        eCount = 15;
        int i = 450;
        while (i++ < 500) {
            // qps 200, 优化吧, totalTime:100ms
            eCount = 15;
//            userId = getUidBookUnit.getRandomUid();
//            bookId = getUidBookUnit.getRandomBook();
//            try {
//                unitId = getUidBookUnit.getRandomUnit(bookId);
//            } catch (NullPointerException e) {
//                unitId = -1L;
//            }
            userId += 1;
            PsrExamContent strRet = utopiaPsrServiceClient.getPsrExam(
                    product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, 3);
        }
        int bbbbb = 0;
    }
}
