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

import com.voxlearning.alps.dao.mysql.persistence.NoCacheStaticMySQLPersistence;
import com.voxlearning.utopia.admin.persist.entity.AdminRole;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.crm.impl.persistence.AdminRolePersistence")
public class AdminRolePersistence extends NoCacheStaticMySQLPersistence<AdminRole, String> {
}
