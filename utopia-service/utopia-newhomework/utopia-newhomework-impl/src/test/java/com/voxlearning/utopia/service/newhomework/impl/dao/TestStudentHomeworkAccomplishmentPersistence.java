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
package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkLocation;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

/**
 * @author xuesong.zhang
 * @since 2016-07-27
 */
@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestStudentHomeworkAccomplishmentPersistence {

    @Inject private StudentHomeworkAccomplishmentPersistence studentHomeworkAccomplishmentPersistence;

    @Test
    @TruncateDatabaseTable(databaseEntities = StudentHomeworkAccomplishment.class)
    public void testFindByLocation() throws Exception {
        HomeworkLocation location = HomeworkLocation.newInstance(Subject.ENGLISH, RandomUtils.nextObjectId());
        assertEquals(0, studentHomeworkAccomplishmentPersistence.findByLocation(location).size());
        int count = 0;
        for (long i = 1; i <= 10; i++) {
            StudentHomeworkAccomplishment accomplishment = StudentHomeworkAccomplishment.mockInstance();
            accomplishment.setHomeworkId(location.getHomeworkId());
            accomplishment.setHomeworkType(HomeworkType.valueOf(NewHomeworkType.Normal.name()));
            accomplishment.setSubject(Subject.ENGLISH);
            accomplishment.setStudentId(i);
            studentHomeworkAccomplishmentPersistence.persist(accomplishment);
            assertEquals(++count, studentHomeworkAccomplishmentPersistence.findByLocation(location).size());
        }
    }
}
