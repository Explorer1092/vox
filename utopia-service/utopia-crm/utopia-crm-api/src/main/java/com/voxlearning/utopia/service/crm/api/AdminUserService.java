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

package com.voxlearning.utopia.service.crm.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.06.07")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AdminUserService {

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<AdminUser> loadAdminUser(String adminUserName);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<AdminUser> loadAdminUserByAgentId(String agentId);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<AdminUser>> findAdminUsersByDepartmentName(String departmentName);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<List<AdminUser>> loadAllAdminUsersIncludeDisabled();

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<AdminUser> persistAdminUser(AdminUser adminUser);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<AdminUser> modifyAdminUser(AdminUser adminUser);
}
