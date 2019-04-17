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
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
@TruncateDatabaseTable(databaseEntities = AdminLog.class)
public class TestAdminLogPersistence {

    @Inject private AdminLogPersistence adminLogPersistence;

    @Test
    public void testAdminLogPersistence() throws Exception {
        AdminLog log = new AdminLog();
        log.setAdminUserName("");
        log.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        log.setOperation("");
        log.setWebActionUrl("");
        log.setComment("");
        adminLogPersistence.insert(log);
        log = adminLogPersistence.load(log.getId());
        assertNotNull(log);
    }
}
