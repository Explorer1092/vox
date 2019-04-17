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

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.admin.persist.entity.AdminPathRole;

import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-16
 * Time: 下午4:01
 * To change this template use File | Settings | File Templates.
 */
@Named
public class AdminPathRolePersistence extends AbstractEntityPersistence<Long, AdminPathRole> {

    public Map<String, Object> getPathRoleByPathRoleId(Long PathRoleId, List<String> appNameList) {
        String sql = "SELECT apr.ID, ap.PATH_NAME, ap.PATH_DESCRIPTION, ap.APP_NAME, apr.ROLE_NAME, apr.PATH_ID "
                + "FROM ADMIN_PATH_ROLE apr " + "JOIN ADMIN_PATH ap ON(ap.id = apr.PATH_ID) "
                + "WHERE apr.id=:a AND apr.DISABLED=0 ";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("a", PathRoleId);

        if (appNameList.size() > 0) {
            sql += " AND ap.APP_NAME IN (:b) ";
            params.put("b", appNameList);
        }

        return getUtopiaSql().withSql(sql).useParams(params).queryRow();
    }

}
