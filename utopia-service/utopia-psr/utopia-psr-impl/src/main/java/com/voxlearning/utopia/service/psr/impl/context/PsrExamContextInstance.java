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

package com.voxlearning.utopia.service.psr.impl.context;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.psr.entity.UserExamContent;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentences;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.service.PsrConfig;
import com.voxlearning.utopia.service.psr.impl.util.PsrBooksPointsRef;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Slf4j
@Named
public class PsrExamContextInstance implements Serializable {
    @Inject private PsrConfig psrConfig;
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    @Inject private PsrExamEnData psrExamEnData;
    @Inject private PsrExamEnFilter psrExamEnFilter;
    @Inject private PsrBooksPointsRef psrBooksPointsRef;
    @Inject private PsrBooksSentences psrBooksSentences;
    @Inject private PsrAboveLevelBookEidsDao psrAboveLevelBookEidsDao;

    public PsrExamContext getPsrExamContext(String product, String uType,
                                            Long userId, int regionCode, String bookId, String unitId, int eCount,
                                            float minP, float maxP, int grade) {

        PsrExamContext psrExamContext = new PsrExamContext(psrConfig,
                product, uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);

        return psrExamContext;
    }

    public PsrExamContext getAndInitPsrExamEnContext(String product, String uType,
                                                     Long userId, int regionCode, String bookId, String unitId, int eCount,
                                                     float minP, float maxP, int grade, Subject subject) {

        PsrExamContext psrExamContext = getPsrExamContext(product,
                uType, userId, regionCode, bookId, unitId, eCount, minP, maxP, grade);

        psrExamContext.setSubject(subject);
        psrExamContext = initPsrExamEnContextData(psrExamContext);

        return psrExamContext;
    }

    // 应试英语 context 初始化 from database
    public PsrExamContext initPsrExamEnContextData(PsrExamContext psrExamContext) {
        if (psrExamContext == null)
            return null;

        // 根据Uid 取出 learning_profile 的 Ek-list, From couchbase
        UserExamContent userExamContent = psrExamEnData.getUserExamContentId(psrExamContext, psrExamContext.getSubject());

        return psrExamContext;
    }

}

