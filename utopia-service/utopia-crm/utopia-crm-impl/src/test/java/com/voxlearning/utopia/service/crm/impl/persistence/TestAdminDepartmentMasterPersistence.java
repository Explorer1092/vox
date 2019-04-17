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

package com.voxlearning.utopia.service.crm.impl.persistence;

import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.admin.persist.entity.AdminDepartmentMaster;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AdminDepartmentMaster.class)
public class TestAdminDepartmentMasterPersistence {

    @Inject private AdminDepartmentMasterPersistence adminDepartmentMasterPersistence;

    @Test
    public void testFindByUserName() throws Exception {
        AdminDepartmentMaster adm = new AdminDepartmentMaster();
        adm.setDepartmentName("");
        adm.setUserName("U");
        adm.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        adminDepartmentMasterPersistence.insert(adm);
        List<AdminDepartmentMaster> list = adminDepartmentMasterPersistence.findByUserName("U");
        assertEquals(1, list.size());
    }
}
