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
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorCompetition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AmbassadorCompetition.class)
public class TestAmbassadorCompetitionDao {

    @Inject
    private AmbassadorCompetitionDao ambassadorCompetitionDao;

    @Test
    public void testUpdateCompetitionSchool() throws Exception {
        AmbassadorCompetition document = new AmbassadorCompetition();
        document.setSubject(Subject.CHINESE);
        ambassadorCompetitionDao.insert(document);
        document = ambassadorCompetitionDao.loadByTeacherId(0L);
        assertEquals(0, document.getSchoolId().longValue());
        assertEquals(1, ambassadorCompetitionDao.loadBySchoolId(0L).size());
        assertEquals(0, ambassadorCompetitionDao.loadBySchoolId(1L).size());
        ambassadorCompetitionDao.updateCompetitionSchool(Subject.CHINESE, 0L, 1L);
        document = ambassadorCompetitionDao.loadByTeacherId(0L);
        assertEquals(1, document.getSchoolId().longValue());
        assertEquals(0, ambassadorCompetitionDao.loadBySchoolId(0L).size());
        assertEquals(1, ambassadorCompetitionDao.loadBySchoolId(1L).size());
    }
}
