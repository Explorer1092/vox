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

package com.voxlearning.utopia.service.newexam.impl.support;

import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.newexam.impl.dao.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
abstract public class NewExamUnitTestSupport extends NewExamSpringBean {
    @Inject
    protected StudentExaminationAuthorityDao studentExaminationAuthorityDao;

    @Inject
    protected RptMockNewExamStudentDao rptMockNewExamStudentDao;
    @Inject
    protected RptMockNewExamClazzDao rptMockNewExamClazzDao;
    @Inject
    protected RptMockNewExamSchoolDao rptMockNewExamSchoolDao;
    @Inject
    protected RptMockNewExamCountyDao rptMockNewExamCountyDao;
}
