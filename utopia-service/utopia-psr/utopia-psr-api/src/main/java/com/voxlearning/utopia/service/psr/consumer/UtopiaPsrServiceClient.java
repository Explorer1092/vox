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

package com.voxlearning.utopia.service.psr.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.psr.api.UtopiaPsrService;
import com.voxlearning.utopia.service.psr.entity.PsrExamContent;
import com.voxlearning.utopia.service.psr.entity.PsrExamEnSimilarContentEx;
import com.voxlearning.utopia.service.psr.entity.PsrPrimaryAppEnContent;
import lombok.Getter;

import java.util.List;

/*
 * @author Chaoli li
 * @since 19:49,2014/06/03.
 */

public class UtopiaPsrServiceClient {

    @Getter @ImportService(interfaceClass = UtopiaPsrService.class) private UtopiaPsrService remoteReference;

    public PsrExamContent getPsrExam(String product, String uType,
                                     Long userId, int regionCode, Long bookId, Long unitId, int eCount,
                                     float minP, float maxP, int grade) {
        return remoteReference.getPsrExam(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);
    }

    public PsrExamContent getPsrExam(String product, String uType,
                                     Long userId, int regionCode, String bookId, String unitId, int eCount,
                                     float minP, float maxP, int grade, Subject subject) {
        return remoteReference.getPsrExam(product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade, subject);
    }


    public PsrExamEnSimilarContentEx getPsrExamEnSimilarByQid(String product, Long userId, List<String> qids, int eCount) {
        return remoteReference.getPsrExamEnSimilarByQid(product, userId, qids, eCount);
    }

    public PsrPrimaryAppEnContent getPsrPrimaryAppEn(String product, Long userId,
                                                     int regionCode, Long bookId, Long unitId, int eCount,
                                                     String eType) {
        return remoteReference.getPsrPrimaryAppEn(product, userId, regionCode, bookId, unitId, eCount, eType);
    }
}

