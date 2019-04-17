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
import com.voxlearning.utopia.admin.persist.entity.AdminAppSystem;
import com.voxlearning.utopia.service.crm.api.AdminAppSystemService;
import com.voxlearning.utopia.service.crm.impl.persistence.AdminAppSystemPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.crm.impl.service.AdminAppSystemServiceImpl")
@ExposeService(interfaceClass = AdminAppSystemService.class)
public class AdminAppSystemServiceImpl extends SpringContainerSupport implements AdminAppSystemService {

    @Inject private AdminAppSystemPersistence adminAppSystemPersistence;

    @Override
    public AlpsFuture<List<AdminAppSystem>> loadAllAdminAppSystems() {
        return new ValueWrapperFuture<>(adminAppSystemPersistence.query());
    }

    @Override
    public AlpsFuture<AdminAppSystem> loadAdminAppSystem(String appName) {
        if (appName == null) {
            return ValueWrapperFuture.nullInst();
        }
        return new ValueWrapperFuture<>(adminAppSystemPersistence.load(appName));
    }

    @Override
    public AlpsFuture<AdminAppSystem> insertAdminAppSystem(AdminAppSystem doc) {
        if (doc == null) {
            return ValueWrapperFuture.nullInst();
        }
        adminAppSystemPersistence.insert(doc);
        return new ValueWrapperFuture<>(doc);
    }

    @Override
    public AlpsFuture<AdminAppSystem> updateAdminAppSystem(AdminAppSystem doc) {
        if (doc == null || doc.getAppName() == null) {
            return ValueWrapperFuture.nullInst();
        }
        AdminAppSystem modified = adminAppSystemPersistence.upsert(doc);
        return new ValueWrapperFuture<>(modified);
    }
}
