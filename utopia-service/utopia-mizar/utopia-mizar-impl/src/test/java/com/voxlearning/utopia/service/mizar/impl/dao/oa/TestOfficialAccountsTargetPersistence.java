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

package com.voxlearning.utopia.service.mizar.impl.dao.oa;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccountsTarget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = OfficialAccountsTarget.class)
public class TestOfficialAccountsTargetPersistence {

    @Inject private OfficialAccountsTargetPersistence officialAccountsTargetPersistence;

    @Test
    public void testFindByAccountId() throws Exception {
        for (int i = 0; i < 3; i++) {
            OfficialAccountsTarget target = new OfficialAccountsTarget();
            target.setAccountId(1L);
            target.setTargetType(0);
            target.setTargetStr("");
            officialAccountsTargetPersistence.insert(target);
            assertEquals(i + 1, officialAccountsTargetPersistence.findByAccountId(1L).size());
        }
    }

    @Test
    public void testClearAccountTarget() throws Exception {
        OfficialAccountsTarget target = new OfficialAccountsTarget();
        target.setAccountId(1L);
        target.setTargetType(0);
        target.setTargetStr("");
        officialAccountsTargetPersistence.insert(target);
        Long id = target.getId();
        officialAccountsTargetPersistence.clearAccountTarget(1L, 0);
        target = officialAccountsTargetPersistence.load(id);
        assertTrue(target.getDisabled());
    }

    @Test
    public void testDisable() throws Exception {
        OfficialAccountsTarget target = new OfficialAccountsTarget();
        target.setAccountId(0L);
        target.setTargetType(0);
        target.setTargetStr("");
        officialAccountsTargetPersistence.insert(target);
        Long id = target.getId();
        officialAccountsTargetPersistence.disable(id);
        target = officialAccountsTargetPersistence.load(id);
        assertTrue(target.getDisabled());
    }
}
