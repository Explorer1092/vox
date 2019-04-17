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

package com.voxlearning.utopia.service.psr.impl.service;

import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentences;
import com.voxlearning.utopia.service.psr.impl.appen.PsrBooksSentencesNew;
import com.voxlearning.utopia.service.psr.impl.context.PsrExamContextInstance;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.dao.PsrAboveLevelBookEidsDao;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnData;
import com.voxlearning.utopia.service.psr.impl.data.PsrExamEnFilter;
import com.voxlearning.utopia.service.psr.impl.examen.PsrExamEnCore;
import com.voxlearning.utopia.service.psr.impl.util.PsrBooksPointsRef;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;

import javax.inject.Inject;
import java.io.Serializable;

//abstract public class PsrServiceSpringBean extends SpringContainerSupport {
abstract public class PsrServiceSpringBean implements Serializable {
    @Inject protected PsrConfig psrConfig;
    @Inject protected EkCouchbaseDao ekCouchbaseDao;
    @Inject protected PsrBooksSentences psrBooksSentences;
    @Inject protected PsrBooksSentencesNew psrBooksSentencesNew;
    @Inject protected PsrBooksPointsRef psrBooksPointsRef;
    @Inject protected PsrAboveLevelBookEidsDao psrAboveLevelBookEidsDao;

    @Inject protected PsrExamEnData psrExamEnData;
    @Inject protected PsrExamEnFilter psrExamEnFilter;
    @Inject protected PsrExamContextInstance psrExamContextInstance;

    @Inject protected PsrExamEnCore psrExamEnCore;

    @Inject protected QuestionLoaderClient questionLoaderClient;

    @Inject protected NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject protected NewContentLoaderClient newContentLoaderClient;
}

