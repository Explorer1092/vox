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
package com.voxlearning.utopia.admin.service.management;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.admin.persist.*;
import com.voxlearning.utopia.admin.persist.entity.*;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.crm.client.AdminAppSystemServiceClient;
import com.voxlearning.utopia.service.crm.client.AdminRoleServiceClient;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Longlong Yu
 * @since 下午5:00,13-11-22.
 */
@Named
public class ManagementService extends AbstractAdminService {

    @Inject private AdminDepartmentMasterPersistence adminDepartmentMasterPersistence;
    @Inject private AdminAppSystemMasterPersistence adminAppSystemMasterPersistence;
    @Inject private AdminPathRolePersistence adminPathRolePersistence;
    @Inject private AdminDepartmentPersistence adminDepartmentPersistence;
    @Inject private AdminGroupMasterPersistence adminGroupMasterPersistence;
    @Inject private AdminGroupPersistence adminGroupPersistence;
    @Inject private AdminPathPersistence adminPathPersistence;

    @Inject private AdminAppSystemServiceClient adminAppSystemServiceClient;
    @Inject private AdminRoleServiceClient adminRoleServiceClient;
    @Inject private AdminUserServiceClient adminUserServiceClient;

    public Map<String, String> getDepartmentMasterNames() {

        List<AdminDepartmentMaster> departmentMasterList = adminDepartmentMasterPersistence.getDepartmentMasters();

        Map<String, List<String>> departmentList = new LinkedHashMap<>();
        for (AdminDepartmentMaster departmentInfo : departmentMasterList) {
            if (!departmentList.containsKey(departmentInfo.getDepartmentName())) {
                departmentList.put(departmentInfo.getDepartmentName(), new ArrayList<String>());
            }
            departmentList.get(departmentInfo.getDepartmentName()).add(departmentInfo.getUserName());
        }
        Map<String, String> ret = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : departmentList.entrySet()) {
            ret.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return ret;
    }

    public Map<String, String> getGroupMasterNames() {
        List<AdminGroupMaster> groupMasterList = adminGroupMasterPersistence.withAllFromTable().queryAll();

        Map<String, List<String>> groupList = new LinkedHashMap<>();
        for (AdminGroupMaster groupInfo : groupMasterList) {
            if (!groupList.containsKey(groupInfo.getGroupName())) {
                groupList.put(groupInfo.getGroupName(), new ArrayList<String>());
            }
            groupList.get(groupInfo.getGroupName()).add(groupInfo.getUserName());
        }
        Map<String, String> ret = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : groupList.entrySet()) {
            ret.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return ret;
    }

    public Map<String, String> getAppMasterNames() {
        List<AdminAppSystemMaster> appMasterList = adminAppSystemMasterPersistence.getAppSystemMasters();

        Map<String, List<String>> appList = new LinkedHashMap<>();
        for (AdminAppSystemMaster appInfo : appMasterList) {
            if (!appList.containsKey(appInfo.getAppName())) {
                appList.put(appInfo.getAppName(), new ArrayList<String>());
            }
            appList.get(appInfo.getAppName()).add(appInfo.getUserName());
        }
        Map<String, String> ret = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : appList.entrySet()) {
            ret.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return ret;
    }

    public Map<String, List<Map<String, String>>> getPathRoles() {

        List<AdminPathRole> pathRoleList = adminPathRolePersistence.withSelectFromTable("*", "WHERE DISABLED=0").queryAll();

        Map<String, List<Map<String, String>>> roleList = new LinkedHashMap<>();
        for (AdminPathRole pathInfo : pathRoleList) {
            if (!roleList.containsKey(pathInfo.getPathId().toString())) {
                roleList.put(pathInfo.getPathId().toString(), new ArrayList<Map<String, String>>());
            }
            Map<String, String> roleMap = new LinkedHashMap<>();
            roleMap.put("roleName", pathInfo.getRoleName());
            roleMap.put("pathRoleId", pathInfo.getId().toString());
            roleList.get(pathInfo.getPathId().toString()).add(roleMap);
        }
        return roleList;
    }

    public Map<String, String> getPathRoleByPathRoleId(Long pathRoleId) {
        List<AdminPathRole> pathRoleList = adminPathRolePersistence.withSelectFromTable("WHERE PATH_ID=?").useParamsArgs(pathRoleId).queryAll();
        Map<String, String> ret = new LinkedHashMap<>();
        Map<String, String> roleNames = getRoleNames();
        for (AdminPathRole pathInfo : pathRoleList) {
            ret.put(pathInfo.getRoleName(), roleNames.get(pathInfo.getRoleName()));
        }
        return ret;
    }

    public Map<String, String> getRoleNames() {
        List<AdminRole> roleList = adminRoleServiceClient.getAdminRoleService().loadAllAdminRoles().getUninterruptibly();
        Map<String, String> ret = new LinkedHashMap<>();
        for (AdminRole roleInfo : roleList) {
            ret.put(roleInfo.getName(), roleInfo.getDescription());
        }
        return ret;
    }

    public Map<String, String> getDepartmentNames() {
        List<AdminDepartment> departmentList = adminDepartmentPersistence.withAllFromTable().queryAll();
        Map<String, String> ret = new LinkedHashMap<>();
        for (AdminDepartment departmentInfo : departmentList) {
            ret.put(departmentInfo.getName(), departmentInfo.getDescription());
        }
        return ret;
    }

    public Map<String, String> getAppNames() {
        List<AdminAppSystem> appList = adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAllAdminAppSystems()
                .getUninterruptibly();
        Map<String, String> ret = new LinkedHashMap<>();
        for (AdminAppSystem appInfo : appList) {
            ret.put(appInfo.getAppName(), appInfo.getAppDescription());
        }
        return ret;
    }

    public Boolean superAdmin(String userName) {
        AdminUser userInfo = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(userName)
                .getUninterruptibly();
        if (userInfo == null) {
            return false;
        }
        return userInfo.getSuperAdmin() && (!userInfo.getDisabled());
    }

    public List<String> getDepartmentList(String userName, String rightType) {
        // FIXME COMMENT BY ZHAO REX for ret may not have been initialized.
        List<String> ret = null;
        if (superAdmin(userName)) {
            ret = adminDepartmentPersistence.withSelectFromTable("NAME", "").queryColumnValues();
        } else {
            if (rightType.equals("read")) {
                ret = adminDepartmentMasterPersistence.getDepartmentListForRead(userName);
            } else if (rightType.equals("write")) {
                ret = adminDepartmentMasterPersistence.getDepartmentListForWrite(userName);
            } else if (rightType.equals("delete")) {
                ret = adminDepartmentMasterPersistence.getDepartmentListForDelete(userName);
            }
        }
        // FIXME COMMENT BY ZHAO REX for ret may not have been initialized.
        return ret;
    }

    public List<String> getGroupList(String userName, String rightType) {
        // FIXME COMMENT BY ZHAO REX for ret may not have been initialized.
        List<String> ret = null;

        if (superAdmin(userName)) {
            ret = adminGroupPersistence.withSelectFromTable("NAME", "").queryColumnValues();
        } else {
            if (rightType.equals("read")) {
                ret = adminGroupMasterPersistence.getGroupListForRead(userName);
            } else if (rightType.equals("write")) {
                ret = adminGroupMasterPersistence.getGroupListForWrite(userName);
            } else if (rightType.equals("delete")) {
                ret = adminGroupMasterPersistence.getGroupListForDelete(userName);
            }
        }

        // FIXME COMMENT BY ZHAO REX for ret may not have been initialized.
        return ret;
    }

    public List<String> getAppList(String userName, String rightType) {
        // FIXME COMMENT BY ZHAO REX for ret may not have been initialized.
        List<String> ret = null;
        if (superAdmin(userName)) {
            ret = adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAllAdminAppSystems()
                    .getUninterruptibly()
                    .stream()
                    .map(AdminAppSystem::getAppName)
                    .collect(Collectors.toList());
        } else {
            if (rightType.equals("read")) {
                ret = adminAppSystemMasterPersistence.getAppListForRead(userName);
            } else if (rightType.equals("write")) {
                ret = adminAppSystemMasterPersistence.getAppListForWrite(userName);
            } else if (rightType.equals("delete")) {
                ret = adminAppSystemMasterPersistence.getAppListForDelete(userName);
            }
        }

        // FIXME COMMENT BY ZHAO REX for ret may not have been initialized.
        return ret;
    }

    public AdminAppSystem getAppInfoByAppName(String appName) {
        AdminAppSystem ret = new AdminAppSystem();
        if (appName.equals("")) {
            return ret;
        }
        return adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAdminAppSystem(appName)
                .getUninterruptibly();
    }

    public List<AdminPath> getPathListByAdmin(String adminName, String pathName) {
        Map<String, Object> params = new HashMap<>();
        if (superAdmin(adminName)) {
            if (StringUtils.isNotBlank(pathName)) {
                params.put("b", pathName);
                return adminPathPersistence.withSelectFromTable("WHERE PATH_NAME=:b").useParams(params).queryAll();
            } else {
                return adminPathPersistence.withAllFromTable().queryAll();
            }
        } else {
            List<String> appNameList = adminAppSystemMasterPersistence.getAppListForWrite(adminName);
            params.put("a", appNameList);
            if (StringUtils.isBlank(pathName)) {
                return adminPathPersistence.withSelectFromTable("WHERE APP_NAME IN (:a)").useParams(params).queryAll();
            } else {
                params.put("b", pathName);
                return adminPathPersistence.withSelectFromTable("WHERE APP_NAME IN (:a) AND PATH_NAME=:b").useParams(params).queryAll();
            }
        }
    }

    /*
    public Pagination getAdminLogList(String adminName, int pageNumber, int pageSize){

        def sql = "SELECT al.ADMIN_USER_NAME, al.TARGET_STR, al.OPERATION, al.CREATE_DATETIME, al.COMMENT, ad.DESCRIPTION FROM ADMIN_LOG al " +
                "JOIN ADMIN_USER au USING(ADMIN_USER_NAME) " +
                "JOIN ADMIN_DEPARTMENT ad ON(ad.NAME=au.DEPARTMENT_NAME) ";
        def sqlCount = "SELECT COUNT(*) FROM ADMIN_LOG";
        //adminLogList = adminLogPersistence.findPageByQuery(sql, sqlCount, null, pageNumber, pageSize);

        Integer total = getUtopiaSql().withSql(sqlCount).queryValue(Integer.class);
        int startIndex = (pageNumber - 1) * pageSize;
        if (startIndex < 0) startIndex = 0;

        List<AdminLog> rows = getUtopiaSql().withSql(sql + " LIMIT " + Integer.toString(startIndex) + "," + Integer.toString(pageSize))
                .useRowMapper(adminLogPersistence.rowMapper).queryAll();

        return new Pagination(pageNumber, pageSize, total, rows, 0);
    }
    */

    //api外部调用
    public Map<String, Object> apiGetUserAppPath(String userName, String appName) {
        Map<String, Object> ret = new HashMap<>();
        ret.put("userName", userName);
        ret.put("isAdmin", false);
        ret.put("pathRight", new ArrayList<>());
        if (userName.equals("") || appName.equals("")) {
            return ret;
        }
        if (apiUserAppAdmin(userName, appName)) {
            ret.put("isAdmin", true);
            return ret;
        }
        ret.put("pathRight", adminPathPersistence.getUserAppPath(userName, appName));
        return ret;
    }

    //api外部调用
    public Boolean apiHasUserAppPathRight(String userName, String appName, String pathName) {
        if (userName.equals("") || appName.equals("") || pathName.equals("")) {
            return false;
        }
        return adminPathPersistence.hasUserAppPathRight(userName, appName, pathName);
    }

    public Boolean apiUserAppAdmin(String userName, String appName) {
        if (superAdmin(userName)) {
            return true;
        } else if (adminAppSystemMasterPersistence.userAppAdmin(userName, appName)) {
            return true;
        }
        return false;
    }

}
