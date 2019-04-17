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
import com.voxlearning.utopia.entity.ambassador.SchoolAmbassador;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.springframework.util.Assert.notNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = SchoolAmbassador.class)
public class TestSchoolAmbassadorPersistence {

    @Inject private SchoolAmbassadorPersistence schoolAmbassadorPersistence;

    @Test
    public void testSchoolAmbassadorPersistence() throws Exception {
        SchoolAmbassador sa = new SchoolAmbassador();
        sa.setUserId(1L);
        sa.setName("");
        sa.setSensitiveMobile("");
        sa.setSensitiveQq("");
        sa.setSensitiveEmail("");
        sa.setLeader("");
        sa.setTotalCount(0);
        sa.setUsingCount(0);
        sa.setSuggestion("");
        schoolAmbassadorPersistence.insert(sa);
        sa = schoolAmbassadorPersistence.load(sa.getId());
        notNull(sa);
        sa = schoolAmbassadorPersistence.findByUserId(1L);
        notNull(sa);
    }
}
