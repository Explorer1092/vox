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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.alps.test.annotation.MockBinder;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.business.api.entity.SmartClazzQuestionReport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author changyuan.liu
 * @since 20151201
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@DropMongoDatabase
public class TestSmartClazzQuestionReportDao {

    @Inject private SmartClazzQuestionReportDao smartClazzQuestionReportDao;

    @Test
    @MockBinder(
            type = SmartClazzQuestionReport.class,
            jsons = {
                    "{'clazzId':1, 'subject':'ENGLISH', 'questionId':'abc'}"
            },
            persistence = SmartClazzQuestionReportDao.class
    )
    public void testFindReportByClazzIdAndQuestionId() {
        SmartClazzQuestionReport report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(1L, Subject.ENGLISH, "abc");
        assertTrue(report != null);

        // from cache
        report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(1L, Subject.ENGLISH, "abc");
        assertTrue(report != null);
    }

    @Test
    public void testInsert() {
        Long clazzId = 1L;
        Subject subject = Subject.ENGLISH;
        String questionId = "abc";

        SmartClazzQuestionReport report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(clazzId, subject, questionId);
        assertTrue(report == null);

        SmartClazzQuestionReport smartClazzQuestionReport = new SmartClazzQuestionReport();
        smartClazzQuestionReport.setClazzId(clazzId);
        smartClazzQuestionReport.setSubject(subject);
        smartClazzQuestionReport.setQuestionId(questionId);
        String id = smartClazzQuestionReportDao.insert(smartClazzQuestionReport);

        assertTrue(id != null);

        report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(clazzId, subject, questionId);
        assertTrue(report != null);
        assertEquals(clazzId, report.getClazzId());
        assertEquals(subject, report.getSubject());
        assertEquals(questionId, report.getQuestionId());
        assertEquals(id, report.getId());
    }

    @Test
    @MockBinder(
            type = SmartClazzQuestionReport.class,
            jsons = {
                    "{'clazzId':1, 'subject':'ENGLISH', 'questionId':'abc'}"
            },
            persistence = SmartClazzQuestionReportDao.class
    )
    public void testUpdate() {
        Long oldClazzId = 1L;
        Long clazzId = 2L;
        Subject subject = Subject.ENGLISH;
        String questionId = "abc";

        SmartClazzQuestionReport report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(oldClazzId, subject, questionId);
        assertTrue(report != null);
        String id = report.getId();
        report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(clazzId, subject, questionId);
        assertTrue(report == null);

        SmartClazzQuestionReport smartClazzQuestionReport = new SmartClazzQuestionReport();
        smartClazzQuestionReport.setId(id);
        smartClazzQuestionReport.setClazzId(clazzId);
        smartClazzQuestionReport.setSubject(subject);
        smartClazzQuestionReport.setQuestionId(questionId);
        smartClazzQuestionReportDao.update(id, smartClazzQuestionReport);

        report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(oldClazzId, subject, questionId);
        assertTrue(report == null);
        report = smartClazzQuestionReportDao.findReportByClazzIdAndQuestionId(clazzId, subject, questionId);
        assertTrue(report != null);
        assertEquals(clazzId, report.getClazzId());
        assertEquals(subject, report.getSubject());
        assertEquals(questionId, report.getQuestionId());
        assertEquals(id, report.getId());
    }
}
