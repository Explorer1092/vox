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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetitionDetail;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AmbassadorCompetitionDetail.class)
public class TestAmbassadorCompetitionDetailDao {

    @Inject
    private AmbassadorCompetitionDetailDao ambassadorCompetitionDetailDao;

    @Test
    public void testLoadByTeacherId() throws Exception {
        long teacherId = 10000;
        for (int i = 0; i < 3; i++) {
            AmbassadorCompetitionDetail detail = new AmbassadorCompetitionDetail();
            detail.setTeacherId(teacherId);
            ambassadorCompetitionDetailDao.insert(detail);
            assertEquals(i + 1, ambassadorCompetitionDetailDao.loadByTeacherId(teacherId).size());
        }
    }

    @Test
    public void testUpdateCompetitionDetailSchool() throws Exception {
        AmbassadorCompetitionDetail detail = new AmbassadorCompetitionDetail();
        detail.setSubject(Subject.ENGLISH);
        detail.setSchoolId(10000L);
        ambassadorCompetitionDetailDao.insert(detail);
        Long id = detail.getId();
        ambassadorCompetitionDetailDao.updateCompetitionDetailSchool(Subject.ENGLISH, 10000L, 20000L);
        detail = ambassadorCompetitionDetailDao.load(id);
        assertEquals(20000, detail.getSchoolId().longValue());
    }
}
