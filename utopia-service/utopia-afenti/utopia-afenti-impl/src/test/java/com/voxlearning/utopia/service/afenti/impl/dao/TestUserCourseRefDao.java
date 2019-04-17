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

package com.voxlearning.utopia.service.afenti.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.afenti.api.entity.UserCourseRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestUserCourseRefDao {

    @Inject
    private UserCourseRefDao userCourseRefDao;

    @Test
    @TruncateDatabaseTable(databaseEntities = UserCourseRef.class)
    public void testUserCourseRef() throws Exception {
        userCourseRefDao.upsertUserCourse(1L, Subject.ENGLISH, "1");
        UserCourseRef userCourseRef1 = userCourseRefDao.findCourseByUserIdAndSubject(1L, Subject.ENGLISH);
        userCourseRefDao.upsertUserCourse(1L, Subject.ENGLISH, "1");
        UserCourseRef userCourseRef2 = userCourseRefDao.findCourseByUserIdAndSubject(1L, Subject.ENGLISH);
        Assert.assertEquals(userCourseRef1.getCreateDatetime().getTime(),userCourseRef2.getCreateDatetime().getTime());
//        userCourseRefDao.upsertUserCourse(1L, Subject.ENGLISH, "2");
//        UserCourseRef userCourseRef3 = userCourseRefDao.findCourseByUserIdAndSubject(1L, Subject.ENGLISH);
//        Assert.assertEquals(userCourseRef1.getCreateDatetime().getTime(),userCourseRef3.getCreateDatetime().getTime());
    }
}
