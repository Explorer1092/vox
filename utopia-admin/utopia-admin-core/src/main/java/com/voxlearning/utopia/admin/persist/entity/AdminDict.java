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

package com.voxlearning.utopia.admin.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Longlong Yu
 * @since 下午5:51,13-10-16.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "admin")
@DocumentTable(table = "ADMIN_DICT")
public class AdminDict extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -507479784165208188L;

    @UtopiaSqlColumn private String groupName;
    @UtopiaSqlColumn private String groupMember;
    @UtopiaSqlColumn private String description;

    public static String ck_groupName(String groupName) {
        return CacheKeyGenerator.generateCacheKey(AdminDict.class, "groupName", groupName);
    }

    public static String ck_all() {
        return CacheKeyGenerator.generateCacheKey(AdminDict.class, "allGroupNames");
    }
}
