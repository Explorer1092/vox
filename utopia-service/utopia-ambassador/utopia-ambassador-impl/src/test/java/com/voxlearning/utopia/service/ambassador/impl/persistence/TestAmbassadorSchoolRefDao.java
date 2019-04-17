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
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorSchoolRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AmbassadorSchoolRef.class)
public class TestAmbassadorSchoolRefDao {

    @Inject
    private AmbassadorSchoolRefDao ambassadorSchoolRefDao;

    @Test
    public void testAmbassadorSchoolRefDao() throws Exception {
        AmbassadorSchoolRef document = new AmbassadorSchoolRef();
        document.setAmbassadorId(1L);
        document.setSchoolId(2L);
        ambassadorSchoolRefDao.insert(document);
        assertEquals(1, ambassadorSchoolRefDao.loadByAmbassadorId(1L).size());
        assertEquals(1, ambassadorSchoolRefDao.findBySchoolId(2L).size());
        ambassadorSchoolRefDao.disabledByAmbassadorId(1L);
        assertEquals(0, ambassadorSchoolRefDao.loadByAmbassadorId(1L).size());
        assertEquals(0, ambassadorSchoolRefDao.findBySchoolId(2L).size());
    }
}
