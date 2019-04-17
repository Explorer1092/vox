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

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.service.crm.api.AdminUserService;
import com.voxlearning.utopia.service.crm.impl.persistence.AdminUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.crm.impl.service.AdminUserServiceImpl")
@ExposeService(interfaceClass = AdminUserService.class)
public class AdminUserServiceImpl extends SpringContainerSupport implements AdminUserService {

    @Inject private AdminUserPersistence adminUserPersistence;

    @Override
    public AlpsFuture<AdminUser> loadAdminUser(String adminUserName) {
        if (adminUserName == null) {
            return new ValueWrapperFuture<>(null);
        }
        return new ValueWrapperFuture<>(adminUserPersistence.load(adminUserName));
    }

    @Override
    public AlpsFuture<AdminUser> loadAdminUserByAgentId(String agentId) {
        if (agentId == null) {
            return new ValueWrapperFuture<>(null);
        }
        return new ValueWrapperFuture<>(adminUserPersistence.loadByAgentId(agentId));
    }

    @Override
    public AlpsFuture<List<AdminUser>> findAdminUsersByDepartmentName(String departmentName) {
        if (departmentName == null) {
            return ValueWrapperFuture.emptyList();
        }
        return new ValueWrapperFuture<>(adminUserPersistence.findByDepartmentName(departmentName));
    }

    @Override
    public AlpsFuture<List<AdminUser>> loadAllAdminUsersIncludeDisabled() {
        return new ValueWrapperFuture<>(adminUserPersistence.query());
    }

    @Override
    public AlpsFuture<AdminUser> persistAdminUser(AdminUser adminUser) {
        adminUserPersistence.insert(adminUser);
        return new ValueWrapperFuture<>(adminUser);
    }

    @Override
    public AlpsFuture<AdminUser> modifyAdminUser(AdminUser adminUser) {
        AdminUser modified = adminUserPersistence.replace(adminUser);
        return new ValueWrapperFuture<>(modified);
    }
}
