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

package com.voxlearning.utopia.service.zone.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = StudentInfo.class)
public class TestStudentInfoPersistence {
    @Autowired private StudentInfoPersistence studentInfoPersistence;

    @Test
    public void testFindByUserIdsOrderByStudyMasterCountDesc() throws Exception {
        {
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(30001L);
            studentInfo.setStudyMasterCount(2);
            studentInfoPersistence.persist(studentInfo);
        }
        {
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(30002L);
            studentInfo.setStudyMasterCount(1);
            studentInfoPersistence.persist(studentInfo);
        }
        {
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(30003L);
            studentInfo.setStudyMasterCount(3);
            studentInfoPersistence.persist(studentInfo);
        }
        List<Long> userIds = Arrays.asList(30001L, 30002L, 30003L);
        List<StudentInfo> studentInfos = studentInfoPersistence.findByUserIdsOrderByStudyMasterCountDesc(new HashSet<>(userIds));
        assertEquals(3, studentInfos.size());
        assertEquals(30003, studentInfos.get(0).getStudentId().longValue());
        assertEquals(30001, studentInfos.get(1).getStudentId().longValue());
        assertEquals(30002, studentInfos.get(2).getStudentId().longValue());

        // second time, 3 student infos loaded from cache
        studentInfos = studentInfoPersistence.findByUserIdsOrderByStudyMasterCountDesc(new HashSet<>(userIds));
        assertEquals(3, studentInfos.size());
        assertEquals(30003, studentInfos.get(0).getStudentId().longValue());
        assertEquals(30001, studentInfos.get(1).getStudentId().longValue());
        assertEquals(30002, studentInfos.get(2).getStudentId().longValue());
    }

    @Test
    public void testFindByUserIdsOrderByLikeCountDesc() throws Exception {
        {
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(30001L);
            studentInfo.setLikeCount(2);
            studentInfoPersistence.persist(studentInfo);
        }
        {
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(30002L);
            studentInfo.setLikeCount(1);
            studentInfoPersistence.persist(studentInfo);
        }
        {
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setStudentId(30003L);
            studentInfo.setLikeCount(3);
            studentInfoPersistence.persist(studentInfo);
        }
        List<Long> userIds = Arrays.asList(30001L, 30002L, 30003L);
        List<StudentInfo> studentInfos = studentInfoPersistence.findByUserIdsOrderByLikeCountDesc(new HashSet<>(userIds));
        assertEquals(3, studentInfos.size());
        assertEquals(30003, studentInfos.get(0).getStudentId().longValue());
        assertEquals(30001, studentInfos.get(1).getStudentId().longValue());
        assertEquals(30002, studentInfos.get(2).getStudentId().longValue());

        // second time, 3 student infos loaded from cache
        studentInfos = studentInfoPersistence.findByUserIdsOrderByLikeCountDesc(new HashSet<>(userIds));
        assertEquals(3, studentInfos.size());
        assertEquals(30003, studentInfos.get(0).getStudentId().longValue());
        assertEquals(30001, studentInfos.get(1).getStudentId().longValue());
        assertEquals(30002, studentInfos.get(2).getStudentId().longValue());
    }

    @Test
    public void testCreateOrIncreaseStudyMasterCountByOne() throws Exception {
        long userId = 30033;
        studentInfoPersistence.createOrIncreaseStudyMasterCountByOne(userId);
        StudentInfo studentInfo = studentInfoPersistence.load(userId);
        assertEquals(1, studentInfo.getStudyMasterCountValue());
        studentInfoPersistence.createOrIncreaseStudyMasterCountByOne(userId);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(2, studentInfo.getStudyMasterCountValue());
        studentInfoPersistence.createOrIncreaseStudyMasterCountByOne(userId);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(3, studentInfo.getStudyMasterCountValue());
    }

    @Test
    public void testCreateOrIncreaseLikeCountByOne() throws Exception {
        long userId = 30033;
        studentInfoPersistence.createOrIncreaseLikeCountByOne(userId);
        StudentInfo studentInfo = studentInfoPersistence.load(userId);
        assertEquals(1, studentInfo.getLikeCountValue());
        studentInfoPersistence.createOrIncreaseLikeCountByOne(userId);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(2, studentInfo.getLikeCountValue());
        studentInfoPersistence.createOrIncreaseLikeCountByOne(userId);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(3, studentInfo.getLikeCountValue());
    }

    @Test
    public void testCreateOrUpdateBubble() throws Exception {
        long userId = 30033;
        studentInfoPersistence.createOrUpdateBubble(userId, 1L);
        StudentInfo studentInfo = studentInfoPersistence.load(userId);
        assertEquals(1, studentInfo.getBubbleId().longValue());
        studentInfoPersistence.createOrUpdateBubble(userId, 2L);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(2, studentInfo.getBubbleId().longValue());
        studentInfoPersistence.createOrUpdateBubble(userId, 3L);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(3, studentInfo.getBubbleId().longValue());
    }

    @Test
    public void testCreateOrIncreaseSignInCountByOne() throws Exception {
        long userId = 30033;
        studentInfoPersistence.createOrIncreaseSignInCountByOne(userId);
        StudentInfo studentInfo = studentInfoPersistence.load(userId);
        assertEquals(1, studentInfo.getSignInCountValue());
        studentInfoPersistence.createOrIncreaseSignInCountByOne(userId);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(2, studentInfo.getSignInCountValue());
        studentInfoPersistence.createOrIncreaseSignInCountByOne(userId);
        studentInfo = studentInfoPersistence.load(userId);
        assertEquals(3, studentInfo.getSignInCountValue());
    }
}
