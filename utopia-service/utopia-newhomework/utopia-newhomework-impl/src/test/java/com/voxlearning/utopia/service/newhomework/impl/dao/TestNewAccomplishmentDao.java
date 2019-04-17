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

package com.voxlearning.utopia.service.newhomework.impl.dao;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.test.DropMongoDatabase;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkUnitTestSupport;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@DropMongoDatabase
public class TestNewAccomplishmentDao extends NewHomeworkUnitTestSupport {

    @Test
    public void testStudentFinished() throws Exception {
        NewAccomplishment.ID id = NewAccomplishment.ID.build(new Date(), Subject.MATH, RandomUtils.nextObjectId());
        NewAccomplishment newAccomplishment = newAccomplishmentDao.load(id.toString());
        assertNull(newAccomplishment);

        newAccomplishmentDao.studentFinished(id, 30009L, null, null, null, "", "");
        newAccomplishment = newAccomplishmentDao.load(id.toString());
        assertTrue(newAccomplishment.getDetails().containsKey("30009"));

        newAccomplishmentDao.studentFinished(id, 30033L, null, null, null, "", "");
        newAccomplishment = newAccomplishmentDao.load(id.toString());
        assertTrue(newAccomplishment.getDetails().containsKey("30009"));
        assertTrue(newAccomplishment.getDetails().containsKey("30033"));
    }
}
