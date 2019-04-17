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

package com.voxlearning.utopia.service.newexam.impl.support;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.newexam.impl.dao.*;
import com.voxlearning.utopia.service.newexam.impl.loader.TikuStrategy;
import com.voxlearning.utopia.service.question.consumer.NewExamLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PaperLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionContentTypeLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;

/**
 * Created by tanguohong on 2016/3/7.
 */
public class NewExamSpringBean extends SpringContainerSupport {

    @Inject protected NewExamCacheClient newExamCacheClient;
    @Inject protected NewExamLoaderClient newExamLoaderClient;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject protected DeprecatedClazzLoaderClient clazzLoaderClient;
    @Inject protected PaperLoaderClient paperLoaderClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected QuestionLoaderClient questionLoaderClient;
    @Inject protected QuestionContentTypeLoaderClient questionContentTypeLoaderClient;

    @Inject protected NewExamResultDao newExamResultDao;
    @Inject protected NewExamProcessResultDao newExamProcessResultDao;
    @Inject protected NewExamRegistrationDao newExamRegistrationDao;
    @Inject protected JournalNewExamProcessResultDao journalNewExamProcessResultDao;

    @Inject protected RptMockNewExamStudentDao rptMockNewExamStudentDao;
    @Inject protected RptMockNewExamCountyDao rptMockNewExamCountyDao;
    @Inject protected RptMockNewExamSchoolDao rptMockNewExamSchoolDao;
    @Inject protected RptMockNewExamClazzDao rptMockNewExamClazzDao ;

    @Inject protected AppMessageServiceClient appMessageServiceClient;
    @Inject protected MessageCommandServiceClient messageCommandServiceClient;
    @Inject protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject protected TikuStrategy tikuStrategy;
    @Inject protected GroupExamRegistrationDao groupExamRegistrationDao;
}
