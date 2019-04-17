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

package com.voxlearning.utopia.service.crm.impl.service;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomStringUtils;
import com.voxlearning.alps.spi.test.TruncateDatabaseTable;
import com.voxlearning.alps.test.runner.AlpsTestRunner;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import com.voxlearning.utopia.service.crm.impl.persistence.AdminUserPersistence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

@RunWith(AlpsTestRunner.class)
@ContextConfiguration(locations = "/UnitTestApplicationContext.xml")
public class TestAdminUserServiceImpl {

    @Inject private AdminUserPersistence adminUserPersistence;
    @Inject private AdminUserServiceClient adminUserServiceClient;

    @Test
    @TruncateDatabaseTable(databaseEntities = AdminUser.class)
    public void testLoadAdminUserByAgentId() throws Exception {
        AdminUser user = new AdminUser();
        user.setAdminUserName(RandomStringUtils.randomAlphabetic(8));
        user.setPassword("");
        user.setPasswordSalt("");
        user.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        user.setRealName("");
        user.setComment("");
        user.setAgentId("A");
        adminUserPersistence.insert(user);
        user = adminUserServiceClient.getAdminUserService().loadAdminUserByAgentId("A").get();
        assertNotNull(user);
    }
}
