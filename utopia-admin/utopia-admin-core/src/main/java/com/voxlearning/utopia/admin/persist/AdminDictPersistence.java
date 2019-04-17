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

package com.voxlearning.utopia.admin.persist;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.admin.persist.entity.AdminDict;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author Longlong Yu
 * @since 下午6:00,13-10-16.
 */
@Named
@UtopiaCacheSupport(AdminDict.class)
public class AdminDictPersistence extends StaticPersistence<Long, AdminDict> {

    @Override
    protected void calculateCacheDimensions(AdminDict source, Collection<String> dimensions) {
        dimensions.add(AdminDict.ck_groupName(source.getGroupName()));
        dimensions.add(AdminDict.ck_all());
    }

    @UtopiaCacheable
    public List<AdminDict> findByGroupName(@UtopiaCacheKey(name = "groupName") String groupName) {
        return withSelectFromTable("WHERE GROUP_NAME=BINARY? AND DISABLED=0").useParamsArgs(groupName).queryAll();
    }

    @UtopiaCacheable(key = "allGroupNames")
    public List<String> findALlGroupName() {
        return getUtopiaSql().withSql("SELECT GROUP_NAME FROM ADMIN_DICT WHERE DISABLED = 0 GROUP BY BINARY GROUP_NAME").queryColumnValues(String.class);
    }

}
