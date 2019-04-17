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

package com.voxlearning.utopia.service.ambassador.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportStudentFeedback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AmbassadorReportStudentFeedback.class)
public class TestAmbassadorReportStudentFeedbackDao {

    @Inject private AmbassadorReportStudentFeedbackDao ambassadorReportStudentFeedbackDao;

    @Test
    public void testAmbassadorReportStudentFeedbackDao() throws Exception {
        AmbassadorReportStudentFeedback document = new AmbassadorReportStudentFeedback();
        document.setTeacherId(1L);
        document.setStudentId(2L);
        document.setConfirm(false);
        ambassadorReportStudentFeedbackDao.insert(document);
        Long id = document.getId();
        assertNotNull(ambassadorReportStudentFeedbackDao.load(id));
        assertEquals(1, ambassadorReportStudentFeedbackDao.loadByTeacherId(1L).size());
        assertEquals(1, ambassadorReportStudentFeedbackDao.loadByTeacherIdAndStudentId(1L, 2L).size());
    }
}
