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
import com.voxlearning.utopia.admin.persist.entity.AdminPath;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-16
 * Time: 下午1:14
 * To change this template use File | Settings | File Templates.
 */
@Named
public class AdminPathPersistence extends AbstractEntityPersistence<Long, AdminPath> {

    public Map<String, List<Object>> getUserAppPath(String userName, String appName) {
        String sql = "SELECT DISTINCT ap.PATH_NAME, apr.ROLE_NAME "
                + "FROM ADMIN_PATH_ROLE_GROUP aprg "
                + "JOIN ADMIN_PATH_ROLE apr ON(aprg.PATH_ROLE_ID=apr.ID AND apr.DISABLED=0) "
                + "JOIN ADMIN_PATH ap ON(apr.PATH_ID = ap.ID) "
                + "JOIN ADMIN_GROUP_USER agu ON(aprg.GROUP_NAME = agu.GROUP_NAME AND agu.DISABLED=0) "
                + "WHERE agu.USER_NAME = ? AND ap.APP_NAME = ? AND aprg.DISABLED=0";
        List<Map<String, Object>> result = getUtopiaSql().withSql(sql).useParamsArgs(userName, appName).queryAll();

        Map<String, List<Object>> ret = new LinkedHashMap<>();

        for (Map<String, Object> r : result) {

            String key = String.valueOf(r.get("PATH_NAME"));
            List<Object> values = ret.get(key);

            if (values == null) {
                values = new ArrayList<>();
                ret.put(key, values);
            }

            values.add(r.get("ROLE_NAME"));
        }

        return ret;
    }

    public Boolean hasUserAppPathRight(String userName, String appName, String pathName) {
        String sql = "SELECT COUNT(*) "
                + "FROM ADMIN_PATH_ROLE_GROUP aprg "
                + "JOIN ADMIN_PATH_ROLE apr ON(aprg.PATH_ROLE_ID=apr.ID AND apr.DISABLED=0) "
                + "JOIN ADMIN_PATH ap ON(apr.PATH_ID = ap.ID) "
                + "JOIN ADMIN_GROUP_USER agu ON(aprg.GROUP_NAME = agu.GROUP_NAME AND agu.DISABLED=0)"
                + "WHERE agu.USER_NAME = ? AND ap.APP_NAME = ? AND ap.PATH_NAME= ? AND aprg.DISABLED=0 ";
        return getUtopiaSql().withSql(sql).useParamsArgs(userName, appName, pathName).queryValue(Long.class) > 0;
    }

    public List<Map<String, Object>> getAppPathRoleByGroup(String groupName) {
        String sql = "SELECT ap.APP_NAME, ap.PATH_NAME, ap.PATH_DESCRIPTION, apr.ROLE_NAME "
                + "FROM ADMIN_PATH_ROLE_GROUP aprg "
                + "JOIN ADMIN_PATH_ROLE apr ON(aprg.PATH_ROLE_ID=apr.ID AND apr.DISABLED=0) "
                + "JOIN ADMIN_PATH ap ON(apr.PATH_ID=ap.ID) "
                + "WHERE aprg.GROUP_NAME=? AND aprg.DISABLED=0 ";
        return getUtopiaSql().withSql(sql).useParamsArgs(groupName).queryAll();
    }

}
