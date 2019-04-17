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

package com.voxlearning.utopia.admin.controller.management;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.admin.persist.entity.*;
import com.voxlearning.utopia.service.crm.client.AdminAppSystemServiceClient;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-5
 * Time: 下午6:16
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management/admin")
public class ManagementAdminController extends ManagementAbstractController {

    @Inject private AdminAppSystemServiceClient adminAppSystemServiceClient;
    @Inject private AdminUserServiceClient adminUserServiceClient;

    @RequestMapping(value = "department_list.vpage", method = RequestMethod.GET)
    public String department_list(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForRead = managementService.getDepartmentList(adminName, "read");
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        if (!managementService.superAdmin(adminName) && departmentListForRead.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        if (!departmentListForRead.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("a", departmentListForRead);
            model.addAttribute("departmentList", adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
        }
        model.addAttribute("departmentListForWrite", departmentListForWrite.toString());
        model.addAttribute("masterList", managementService.getDepartmentMasterNames());
        return "management/admin/department_list";
    }

    @RequestMapping(value = "department_new.vpage", method = RequestMethod.GET)
    public String department_new(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        Boolean showAdmin = managementService.superAdmin(adminName);
        if (!showAdmin) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("showAdmin", showAdmin);
        model.addAttribute("includeUrl", "admin/department_form.ftl");
        model.addAttribute("pageMessage", "新建部门");
        return "management/form";
    }

    @RequestMapping(value = "department_new.vpage", method = RequestMethod.POST)
    public String department_new_post(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        Boolean showAdmin = managementService.superAdmin(adminName);
        if (!showAdmin) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("showAdmin", showAdmin);
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String departmentDescription = getRequestParameter("departmentDescription", "").replaceAll("\\s", "");
        if (departmentName.equals("")) {
            getAlertMessageManager().addMessageError("部门标识不能为空");
        }
        if (!departmentName.matches("^[a-zA-Z]+")) {
            getAlertMessageManager().addMessageError("部门标识只能由英文组成");
        }
        if (departmentName.length() > 50) {
            getAlertMessageManager().addMessageError("部门标识符过长");
        }
        if (departmentDescription.length() > 255) {
            getAlertMessageManager().addMessageError("部门中文名称过长");
        }
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "admin/department_form.ftl");
            model.addAttribute("pageMessage", "新建部门");
            model.addAttribute("departmentName", departmentName);
            model.addAttribute("departmentDescription", departmentDescription);
            return "management/form";
        }
        AdminDepartment departmentInfo = adminDepartmentPersistence.withSelectFromTable("WHERE NAME=? ").useParamsArgs(departmentName).queryObject();
        if (departmentInfo != null) {
            getAlertMessageManager().addMessageError("此部门标识已存在");
            model.addAttribute("includeUrl", "admin/department_form.ftl");
            model.addAttribute("departmentName", departmentName);
            model.addAttribute("departmentDescription", departmentDescription);
            model.addAttribute("pageMessage", "新建部门");
            return "management/form";
        } else {
            //新建部门
            AdminDepartment newDepartment = new AdminDepartment();
            newDepartment.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newDepartment.setName(departmentName);
            newDepartment.setDescription(departmentDescription);
            adminDepartmentPersistence.persist(newDepartment);
            addAdminLog("addNewDepartment", departmentName, departmentDescription);
            getAlertMessageManager().addMessageSuccess("添加新部门" + departmentDescription + "成功");
            return redirect("/management/admin/department_list.vpage");
        }
    }

    @RequestMapping(value = "department_admin.vpage", method = RequestMethod.GET)
    public String department_admin(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        String departmentName = getRequestParameter("name", "").replaceAll("\\s", "");
        if (!departmentListForWrite.contains(departmentName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        List<String> departmentListForDelete = managementService.getDepartmentList(adminName, "delete");
        boolean rightDelete = departmentListForDelete.contains(departmentName) ? true : false;

        // FIXME COMMENT BY ZHAO REX FOR departmentInfo may not have been initialized.
        AdminDepartment departmentInfo = null;
        // FIXME COMMENT BY ZHAO REX FOR masterList may not have been initialized.
        List<AdminDepartmentMaster> masterList = null;
        if (StringUtils.isNotBlank(departmentName)) {
            departmentInfo = adminDepartmentPersistence.loadFromDatabase(departmentName);
            masterList = adminDepartmentMasterPersistence.withSelectFromTable("WHERE DEPARTMENT_NAME=? ").useParamsArgs(departmentName).queryAll();
        }
        model.addAttribute("pageMessage", "部门管理");
        // FIXME COMMENT BY ZHAO REX FOR departmentInfo may not have been initialized.
        model.addAttribute("departmentInfo", departmentInfo);
        // FIXME COMMENT BY ZHAO REX FOR masterList may not have been initialized.
        model.addAttribute("masterList", masterList);
        model.addAttribute("rightDelete", rightDelete);
        return "management/admin/department_admin";
    }

    @RequestMapping(value = "department_admin.vpage", method = RequestMethod.POST)
    public String department_admin_post() {
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        if (!departmentListForWrite.contains(departmentName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        if (userName.equals("")) {
            getAlertMessageManager().addMessageError("用户名不能为空");
            return redirect("/management/admin/department_admin.vpage?name=" + departmentName);
        }
        AdminDepartment departmentInfo = adminDepartmentPersistence.loadFromDatabase(departmentName);
        if (departmentInfo == null) {
            getAlertMessageManager().addMessageError("部门不存在");
            return redirect("/management/admin/department_admin.vpage?name=" + departmentName);
        }
        List<AdminDepartmentMaster> masterList = adminDepartmentMasterPersistence.withSelectFromTable("WHERE DEPARTMENT_NAME=? AND USER_NAME=?")
                .useParamsArgs(departmentName, userName).queryAll();
        if (CollectionUtils.isNotEmpty(masterList)) {
            getAlertMessageManager().addMessageError("管理员已存在");
            return redirect("/management/admin/department_admin.vpage?name=" + departmentName);
        }

        AdminDepartmentMaster newDepartmentMaster = new AdminDepartmentMaster();
        newDepartmentMaster.setDepartmentName(departmentName);
        newDepartmentMaster.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        newDepartmentMaster.setUserName(userName);
        adminDepartmentMasterPersistence.persist(newDepartmentMaster);
        addAdminLog("addMasterToDepartment", userName, departmentName, newDepartmentMaster);
        getAlertMessageManager().addMessageSuccess("添加新管理员" + userName + "成功");
        return redirect("/management/admin/department_admin.vpage?name=" + departmentName);
    }

    @RequestMapping(value = "department_admin_edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object department_admin_edit() {
        String doType = getRequestParameter("do", "").replaceAll("\\s", "");
        String action = getRequestParameter("action", "").replaceAll("\\s", "");
        long adminMasterId = Long.parseLong(getRequestParameter("adminMasterId", "0"));
        AdminDepartmentMaster adminMasterInfo = adminDepartmentMasterPersistence.loadFromDatabase(adminMasterId);
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");

        //检查权限
        if (departmentListForWrite.contains(adminMasterInfo.getDepartmentName())) {
            AdminDepartmentMaster editDepartmentMaster = new AdminDepartmentMaster();
            if (doType.equals("add")) {
                // FIXME COMMENT BY ZHAO REX AdminDepartmentMaster has no field action.
//                editDepartmentMaster[action] = true;
                adminDepartmentMasterPersistence.update(adminMasterId, editDepartmentMaster);
                addAdminLog("addDepartmentRightToMaster", adminMasterId, action, editDepartmentMaster);
            } else if (doType.equals("del")) {
                // FIXME COMMENT BY ZHAO REX AdminDepartmentMaster has no field action.
//                editDepartmentMaster[action] = false;
                adminDepartmentMasterPersistence.update(adminMasterId, editDepartmentMaster);
                addAdminLog("delDepartmentRightToMaster", adminMasterId, action, editDepartmentMaster);
            }
        }

        // FIXME COMMENT BY ZHAO REX What is the return value?
        return null;
    }

    @RequestMapping(value = "group_list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String group_list(Model model) {
        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForRead = managementService.getGroupList(adminName, "read");
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        if (!managementService.superAdmin(adminName) && groupListForRead.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        if (groupName.equals("") && !groupListForRead.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("a", groupListForRead);
            model.addAttribute("groupList", adminGroupPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
        } else {
            if (groupListForRead.contains(groupName)) {
                model.addAttribute("groupList", adminGroupPersistence.withSelectFromTable("WHERE NAME=?").useParamsArgs(groupName).queryAll());
            } else {
                if (!managementService.superAdmin(adminName))
                    getAlertMessageManager().addMessageError("您缺少权限查看此权限组");
                if (!groupListForRead.isEmpty()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("a", groupListForRead);
                    model.addAttribute("groupList", adminGroupPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
                }
            }

        }
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("masterList", managementService.getGroupMasterNames());
        model.addAttribute("pageMessage", "权限组");
        model.addAttribute("groupName", groupName);
        model.addAttribute("groupListForWrite", groupListForWrite.toString());
        return "management/admin/group_list";
    }

    @RequestMapping(value = "group_admin_edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public String group_admin_edit() {
        String doType = getRequestParameter("do", "").replaceAll("\\s", "");
        String action = getRequestParameter("action", "").replaceAll("\\s", "");
        long adminMasterId = Long.parseLong(getRequestParameter("adminMasterId", "0"));
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        AdminGroupMaster adminMasterInfo = adminGroupMasterPersistence.loadFromDatabase(adminMasterId);
        if (groupListForWrite.contains(adminMasterInfo.getGroupName())) {
            AdminGroupMaster editGroupMaster = new AdminGroupMaster();
            if (doType.equals("add")) {
                // FIXME COMMENT BY ZHAO REX AdminDepartmentMaster has no field action.
//                editGroupMaster[action] = true;
                adminGroupMasterPersistence.update(adminMasterId, editGroupMaster);
                addAdminLog("addGroupRightToMaster", adminMasterId, action, editGroupMaster);
            } else if (doType.equals("del")) {
                // FIXME COMMENT BY ZHAO REX AdminDepartmentMaster has no field action.
//                editGroupMaster[action] = false;
                adminGroupMasterPersistence.update(adminMasterId, editGroupMaster);
                addAdminLog("delGroupRightToMaster", adminMasterId, action, editGroupMaster);
            }
        }

        // FIXME COMMENT BY ZHAO REX What is the return value?
        return null;
    }

    @RequestMapping(value = "group_admin.vpage", method = RequestMethod.GET)
    public String group_admin(Model model) {
        String groupName = getRequestParameter("name", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        List<String> groupListForDelete = managementService.getGroupList(adminName, "delete");
        boolean rightDelete = groupListForDelete.contains(groupName) ? true : false;
        if (!groupListForWrite.contains(groupName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        AdminGroup groupInfo = adminGroupPersistence.loadFromDatabase(groupName);
        List<AdminGroupMaster> masterList = adminGroupMasterPersistence.withSelectFromTable("WHERE GROUP_NAME=?").useParamsArgs(groupName).queryAll();
        model.addAttribute("groupInfo", groupInfo);
        model.addAttribute("masterList", masterList);
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("pageMessage", "权限组 - 管理员权限管理");
        model.addAttribute("rightDelete", rightDelete);
        return "management/admin/group_admin";
    }

    @RequestMapping(value = "group_admin.vpage", method = RequestMethod.POST)
    public String group_admin_post() {
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        if (!groupListForWrite.contains(groupName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        if (userName.equals("")) {
            getAlertMessageManager().addMessageError("用户名不能为空");
            return redirect("/management/admin/group_admin.vpage?name=" + groupName);
        }
        AdminGroup groupInfo = adminGroupPersistence.loadFromDatabase(groupName);
        if (groupInfo == null) {
            getAlertMessageManager().addMessageError("权限组不存在");
            return redirect("/management/admin/group_admin.vpage?name=" + groupName);
        }
        AdminGroupMaster masterInfo = adminGroupMasterPersistence.withSelectFromTable("WHERE GROUP_NAME=? AND USER_NAME=? ").useParamsArgs(groupName, userName).queryObject();

        AdminGroupMaster newGroupMaster = new AdminGroupMaster();
        if (masterInfo != null) {
            getAlertMessageManager().addMessageError("管理员已存在");
            return redirect("/management/admin/group_admin.vpage?name=" + groupName);
        } else {
            newGroupMaster.setGroupName(groupName);
            newGroupMaster.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newGroupMaster.setUserName(userName);
            adminGroupMasterPersistence.persist(newGroupMaster);
            addAdminLog("addMasterToGroup", userName, groupName, newGroupMaster);
            getAlertMessageManager().addMessageSuccess("添加新管理员" + userName + "成功");
        }
        return redirect("/management/admin/group_admin.vpage?name=" + groupName);
    }

    @RequestMapping(value = "user_list.vpage", method = RequestMethod.GET)
    public String user_list(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("pageMessage", "用户");
        model.addAttribute("departmentList", adminDepartmentPersistence.withAllFromTable().queryAll());
        List<AdminUser> userList;
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        if (StringUtils.isNotBlank(departmentName)) {
            // FIXME: 为什么这里只取了一个用户？原代码是这样的：
            // FIXME: AdminUser adminUser = adminUserPersistence.withSelectFromTable("WHERE DEPARTMENT_NAME=? ")
            // FIXME: .useParamsArgs(departmentName).queryObject();
            AdminUser adminUser = adminUserServiceClient.getAdminUserService()
                    .loadAllAdminUsersIncludeDisabled()
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> StringUtils.equals(departmentName, e.getDepartmentName()))
                    .findFirst()
                    .orElse(null);
            userList = (adminUser == null) ? new ArrayList<AdminUser>() : Arrays.asList(adminUser);
        } else {
            userList = adminUserServiceClient.getAdminUserService()
                    .loadAllAdminUsersIncludeDisabled()
                    .getUninterruptibly();
        }
        model.addAttribute("userList", userList);
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/admin/user_list";
    }

    @RequestMapping(value = "app_list.vpage", method = RequestMethod.GET)
    public String app_list(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> appListForRead = managementService.getAppList(adminName, "read");
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        if (!managementService.superAdmin(adminName) && appListForRead.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        if (!appListForRead.isEmpty()) {
            List<AdminAppSystem> appList = adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAllAdminAppSystems()
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> appListForRead.contains(e.getAppName()))
                    .collect(Collectors.toList());
            model.addAttribute("appList", appList);
        }
        model.addAttribute("masterList", managementService.getAppMasterNames());
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("appListForWrite", appListForWrite.toString());
        return "management/admin/app_list";
    }

    @RequestMapping(value = "app_new.vpage", method = RequestMethod.GET)
    public String app_new(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        if (!managementService.superAdmin(adminName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("includeUrl", "admin/app_form.ftl");
        model.addAttribute("pageMessage", "添加业务系统");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/form";
    }

    @RequestMapping(value = "app_new.vpage", method = RequestMethod.POST)
    public String app_new_post(Model model) {
        String appName = getRequestParameter("appName", "").replaceAll("\\s", "");
        String appDescription = getRequestParameter("appDescription", "").replaceAll("\\s", "");
        String callBackUrl = getRequestParameter("callBackUrl", "").replaceAll("\\s", "");
        String appKey = getRequestParameter("appKey", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        if (!managementService.superAdmin(adminName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        if (appName.equals("")) {
            getAlertMessageManager().addMessageError("系统标识不能为空");
        }
        if (!appName.matches("^[a-zA-Z]+")) {
            getAlertMessageManager().addMessageError("系统标识只能由英文组成");
        }
        if (appName.length() > 50) {
            getAlertMessageManager().addMessageError("系统标识符过长");
        }
        if (appDescription.length() > 255) {
            getAlertMessageManager().addMessageError("系统中文名称过长");
        }
        if (callBackUrl.equals("")) {
            getAlertMessageManager().addMessageError("回调Url不能为空");
        }
        if (appKey.equals("")) {
            getAlertMessageManager().addMessageError("系统KEY不能为空");
        }
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "admin/app_form.ftl");
            model.addAttribute("pageMessage", "添加业务系统");
            model.addAttribute("appName", appName);
            model.addAttribute("appDescription", appDescription);
            model.addAttribute("callBackUrl", callBackUrl);
            model.addAttribute("appKey", appKey);
            return "management/form";
        }
        AdminAppSystem appInfo = adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAdminAppSystem(appName)
                .getUninterruptibly();
        if (appInfo != null) {
            getAlertMessageManager().addMessageError("此系统标识已存在");
            model.addAttribute("includeUrl", "admin/app_form.ftl");
            model.addAttribute("pageMessage", "添加业务系统");
            model.addAttribute("appName", appName);
            model.addAttribute("appDescription", appDescription);
            model.addAttribute("callBackUrl", callBackUrl);
            model.addAttribute("appKey", appKey);
            return "management/form";
        } else {
            //新建业务系统
            AdminAppSystem newAppSystem = new AdminAppSystem();
            newAppSystem.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newAppSystem.setAppName(appName);
            newAppSystem.setAppDescription(appDescription);
            newAppSystem.setCallBackUrl(callBackUrl);
            newAppSystem.setAppKey(appKey);
            newAppSystem = adminAppSystemServiceClient.getAdminAppSystemService()
                    .insertAdminAppSystem(newAppSystem)
                    .getUninterruptibly();
            addAdminLog("addNewApp", appName, appDescription, newAppSystem);
            getAlertMessageManager().addMessageSuccess("添加新业务系统" + appDescription + "成功");
            return redirect("/management/admin/app_list.vpage");
        }
    }

    @RequestMapping(value = "app_edit.vpage", method = RequestMethod.GET)
    public String app_edit(Model model) {
        String appName = getRequestParameter("name", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        if (!managementService.superAdmin(adminName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }

        // FIXME COMMENT BY ZHAO REX FOR appInfo may not have been initiazlied.
        AdminAppSystem appInfo = null;
        if (StringUtils.isNotEmpty(appName)) {
            appInfo = adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAdminAppSystem(appName)
                    .getUninterruptibly();
            if (appInfo != null) {
                model.addAttribute("appName", appInfo.getAppName());
                model.addAttribute("appDescription", appInfo.getAppDescription());
                model.addAttribute("callBackUrl", appInfo.getCallBackUrl());
                model.addAttribute("appKey", appInfo.getAppKey());
                model.addAttribute("editPage", true);
            }
        }

        // FIXME COMMENT BY ZHAO REX FOR appInfo may not have been initiazlied.
        if (appInfo == null) {
            getAlertMessageManager().addMessageError("您访问的页面不存在");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("includeUrl", "admin/app_form.ftl");
        model.addAttribute("pageMessage", "修改业务系统");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/form";
    }

    @RequestMapping(value = "app_edit.vpage", method = RequestMethod.POST)
    public String app_edit_post(Model model) {
        String appName = getRequestParameter("appName", "").replaceAll("\\s", "");
        String appDescription = getRequestParameter("appDescription", "").replaceAll("\\s", "");
        String callBackUrl = getRequestParameter("callBackUrl", "").replaceAll("\\s", "");
        String appKey = getRequestParameter("appKey", "").replaceAll("\\s", "");

        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        if (!managementService.superAdmin(adminName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }

        if (appName.equals("")) {
            getAlertMessageManager().addMessageError("系统标识不能为空");
        }
        if (!appName.matches("^[a-zA-Z]+")) {
            getAlertMessageManager().addMessageError("系统标识只能由英文组成");
        }
        if (appName.length() > 50) {
            getAlertMessageManager().addMessageError("系统标识符过长");
        }
        if (appDescription.length() > 255) {
            getAlertMessageManager().addMessageError("系统中文名称过长");
        }
        if (callBackUrl.equals("")) {
            getAlertMessageManager().addMessageError("回调Url不能为空");
        }
        if (appKey.equals("")) {
            getAlertMessageManager().addMessageError("系统KEY不能为空");
        }
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "admin/app_form.ftl");
            model.addAttribute("pageMessage", "修改业务系统");
            model.addAttribute("appName", appName);
            model.addAttribute("editPage", true);
            model.addAttribute("appDescription", appDescription);
            model.addAttribute("callBackUrl", callBackUrl);
            model.addAttribute("appKey", appKey);
            return "management/form";
        }

        AdminAppSystem appInfo = adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAdminAppSystem(appName)
                .getUninterruptibly();
        if (appInfo != null) {
            //修改业务系统
            AdminAppSystem newAppSystem = new AdminAppSystem();
            newAppSystem.setAppName(appName);
            newAppSystem.setAppDescription(appDescription);
            newAppSystem.setCallBackUrl(callBackUrl);
            newAppSystem.setAppKey(appKey);
            newAppSystem = adminAppSystemServiceClient.getAdminAppSystemService()
                    .updateAdminAppSystem(newAppSystem)
                    .getUninterruptibly();
            addAdminLog("editApp", appName, appDescription, newAppSystem);
            getAlertMessageManager().addMessageSuccess("修改业务系统" + appDescription + "成功");
            return redirect("/management/admin/app_list.vpage");
        } else {
            getAlertMessageManager().addMessageError("要修改的业务系统不存在，请先建立业务系统");
            return redirect("/management/admin/app_list.vpage");
        }
    }


    @RequestMapping(value = "app_admin.vpage", method = RequestMethod.GET)
    public String app_admin(Model model) {
        String appName = getRequestParameter("name", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        List<String> appListForDelete = managementService.getAppList(adminName, "delete");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        if (!appListForWrite.contains(appName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        boolean rightDelete = appListForDelete.contains(appName) ? true : false;

        // FIXME COMMENT BY ZHAO REX FOR appInfo may not have been initialized.
        AdminAppSystem appInfo = null;
        // FIXME COMMENT BY ZHAO REX FOR masterList may not have been initialized.
        List<AdminAppSystemMaster> masterList = null;
        if (StringUtils.isNotBlank(appName)) {
            appInfo = adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAdminAppSystem(appName)
                    .getUninterruptibly();
            masterList = adminAppSystemMasterPersistence.withSelectFromTable("WHERE APP_NAME=? ").useParamsArgs(appName).queryAll();
        }
        model.addAttribute("pageMessage", "业务系统 - 管理员权限管理");
        // FIXME COMMENT BY ZHAO REX FOR appInfo may not have been initialized.
        model.addAttribute("appInfo", appInfo);
        // FIXME COMMENT BY ZHAO REX FOR masterList may not have been initialized.
        model.addAttribute("masterList", masterList);
        model.addAttribute("rightDelete", rightDelete);
        return "management/admin/app_admin";
    }

    @RequestMapping(value = "app_admin.vpage", method = RequestMethod.POST)
    public String app_admin_post() {
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String appName = getRequestParameter("appName", "").replaceAll("\\s", "");
        if (userName.equals("")) {
            getAlertMessageManager().addMessageError("用户名不能为空");
            return redirect("/management/admin/app_admin.vpage?name=" + appName);
        }

        AdminAppSystem appInfo = adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAdminAppSystem(appName)
                .getUninterruptibly();
        if (appInfo == null) {
            getAlertMessageManager().addMessageError("业务系统不存在");
            return redirect("/management/admin/app_admin.vpage?name=" + appName);
        }
        List<AdminAppSystemMaster> masterList = adminAppSystemMasterPersistence.withSelectFromTable("WHERE APP_NAME=? AND USER_NAME=?").useParamsArgs(appName, userName).queryAll();
        if (CollectionUtils.isNotEmpty(masterList)) {
            getAlertMessageManager().addMessageError("管理员已存在");
            return redirect("/management/admin/app_admin.vpage?name=" + appName);
        }

        AdminAppSystemMaster newAppMaster = new AdminAppSystemMaster();
        newAppMaster.setAppName(appName);
        newAppMaster.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        newAppMaster.setUserName(userName);
        adminAppSystemMasterPersistence.persist(newAppMaster);
        addAdminLog("addMasterToApp", userName, appName, newAppMaster);
        getAlertMessageManager().addMessageSuccess("添加新管理员" + userName + "成功");
        return redirect("/management/admin/app_admin.vpage?name=" + appName);
    }

    @RequestMapping(value = "app_admin_edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object app_admin_edit() {
        String doType = getRequestParameter("do", "").replaceAll("\\s", "");
        String action = getRequestParameter("action", "").replaceAll("\\s", "");
        long adminMasterId = Long.parseLong(getRequestParameter("adminMasterId", "0"));

        AdminAppSystemMaster adminMasterInfo = adminAppSystemMasterPersistence.loadFromDatabase(adminMasterId);
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> appListForDelete = managementService.getAppList(adminName, "delete");
        if (appListForDelete.contains(adminMasterInfo.getAppName())) {
            AdminAppSystemMaster editAppMaster = new AdminAppSystemMaster();
            if (doType.equals("add")) {
                // FIXME COMMENT BY ZHAO REX FOR AdminAppSystemMaster has no field action.
//                editAppMaster[action] = true;
                adminAppSystemMasterPersistence.update(adminMasterId, editAppMaster);
                addAdminLog("addAppRightToMaster", adminMasterId, action, editAppMaster);
            } else if (doType.equals("del")) {
                // FIXME COMMENT BY ZHAO REX FOR AdminAppSystemMaster has no field action.
//                editAppMaster[action] = false;
                adminAppSystemMasterPersistence.update(adminMasterId, editAppMaster);
                addAdminLog("delAppRightToMaster", adminMasterId, action, editAppMaster);
            }
        }

        // FIXME COMMENT BY ZHAO REX What is the return value?
        return null;
    }
}
