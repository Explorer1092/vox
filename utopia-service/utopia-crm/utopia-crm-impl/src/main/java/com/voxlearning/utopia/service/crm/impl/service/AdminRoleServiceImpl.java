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
import com.voxlearning.utopia.admin.persist.entity.AdminRole;
import com.voxlearning.utopia.service.crm.api.AdminRoleService;
import com.voxlearning.utopia.service.crm.impl.persistence.AdminRolePersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.crm.impl.service.AdminRoleServiceImpl")
@ExposeService(interfaceClass = AdminRoleService.class)
public class AdminRoleServiceImpl extends SpringContainerSupport implements AdminRoleService {

    @Inject private AdminRolePersistence adminRolePersistence;

    @Override
    public AlpsFuture<List<AdminRole>> loadAllAdminRoles() {
        return new ValueWrapperFuture<>(adminRolePersistence.query());
    }

    @Override
    public AlpsFuture<AdminRole> loadAdminRole(String name) {
        if (name == null) {
            return ValueWrapperFuture.nullInst();
        }
        return new ValueWrapperFuture<>(adminRolePersistence.load(name));
    }

    @Override
    public AlpsFuture<AdminRole> insertAdminRole(AdminRole adminRole) {
        if (adminRole == null) {
            return ValueWrapperFuture.nullInst();
        }
        adminRolePersistence.insert(adminRole);
        return new ValueWrapperFuture<>(adminRole);
    }
}
