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
import com.voxlearning.utopia.admin.persist.entity.AdminLog;
import com.voxlearning.utopia.service.crm.api.AdminLogService;
import com.voxlearning.utopia.service.crm.impl.persistence.AdminLogPersistence;

import javax.inject.Inject;
import javax.inject.Named;

@Named("com.voxlearning.utopia.service.crm.impl.service.AdminLogServiceImpl")
@ExposeService(interfaceClass = AdminLogService.class)
public class AdminLogServiceImpl extends SpringContainerSupport implements AdminLogService {

    @Inject private AdminLogPersistence adminLogPersistence;

    @Override
    public AlpsFuture<AdminLog> persistAdminLog(AdminLog adminLog) {
        adminLogPersistence.insert(adminLog);
        return new ValueWrapperFuture<>(adminLog);
    }
}
