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

import com.voxlearning.alps.api.concurrent.AlpsFutureBuilder;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.persist.entity.*;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-5
 * Time: 下午4:59
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management/group")
public class ManagementGroupController extends ManagementAbstractController {

    @Inject private AdminUserServiceClient adminUserServiceClient;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForRead = managementService.getGroupList(adminName, "read");
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        List<String> groupListForDelete = managementService.getGroupList(adminName, "delete");
        if (!managementService.superAdmin(adminName) && groupListForRead.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        List<AdminGroup> selectGroupList = new ArrayList<>();
        if (!groupListForRead.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("a", groupListForRead);
            selectGroupList = adminGroupPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll();
        }
        List<AdminGroup> groupList = null;
        if (groupName.equals("") && !groupListForRead.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("a", groupListForRead);
            groupList = adminGroupPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll();
        } else {
            if (groupListForRead.contains(groupName)) {
                groupList = adminGroupPersistence.withSelectFromTable("WHERE NAME=?").useParamsArgs(groupName).queryAll();
            } else {
                if (!managementService.superAdmin(adminName))
                    getAlertMessageManager().addMessageError("您缺少权限查看此权限组");
                if (!groupListForRead.isEmpty())
                    groupList = adminGroupPersistence.withSelectFromTable("WHERE NAME IN (?)").useParams(groupListForRead).queryAll();
            }
        }
        model.addAttribute("groupName", groupName);
        model.addAttribute("groupList", groupList);
        model.addAttribute("selectGroupList", selectGroupList);
        model.addAttribute("groupForWrite", groupListForWrite.toString());
        model.addAttribute("groupForDelete", groupListForDelete.toString());
        model.addAttribute("pageMessage", "权限组");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/group/list";
    }

    @RequestMapping(value = "group_new.vpage", method = RequestMethod.GET)
    public String group_new(Model model) {
        model.addAttribute("pageMessage", "添加权限组");
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("includeUrl", "group/group_form.ftl");
        return "management/form";
    }

    @RequestMapping(value = "group_new.vpage", method = RequestMethod.POST)
    public String group_new_post(Model model) {
        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
        String groupDescription = getRequestParameter("groupDescription", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        if (groupName.equals("")) {
            getAlertMessageManager().addMessageError("组标识不能为空");
        }
        if (!groupName.matches("^[a-zA-Z]+")) {
            getAlertMessageManager().addMessageError("组标识只能由英文组成");
        }
        if (groupName.length() > 50) {
            getAlertMessageManager().addMessageError("组标识符过长");
        }
        if (groupDescription.length() > 255) {
            getAlertMessageManager().addMessageError("组中文名称过长");
        }
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "group/group_form.ftl");
            model.addAttribute("pageMessage", "添加权限组");
            model.addAttribute("groupName", groupName);
            model.addAttribute("showAdmin", managementService.superAdmin(adminName));
            model.addAttribute("groupDescription", groupDescription);
            return "management/form";
        }
        boolean existGroup = adminGroupPersistence.exists(groupName);
        if (existGroup) {
            getAlertMessageManager().addMessageError("已存在相同的权限组标识");
            model.addAttribute("includeUrl", "group/group_form.ftl");
            model.addAttribute("groupName", groupName);
            model.addAttribute("groupDescription", groupDescription);
            model.addAttribute("pageMessage", "添加权限组");
            model.addAttribute("showAdmin", managementService.superAdmin(adminName));
            return "management/form";
        } else {
            //新建权限组
            AdminGroup newGroup = new AdminGroup();
            newGroup.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newGroup.setName(groupName);
            newGroup.setDescription(groupDescription);
            adminGroupPersistence.persist(newGroup);
            addAdminLog("addNewGroup", groupName);
            getAlertMessageManager().addMessageSuccess("添加新权限组" + groupDescription + "成功");
            return redirect("/management/group/list.vpage");
        }
    }

    @RequestMapping(value = "group_edit.vpage", method = RequestMethod.GET)
    public String group_edit(Model model) {
        String groupName = getRequestParameter("name", "").replaceAll("\\s", "");
        AdminGroup groupInfo = adminGroupPersistence.loadFromDatabase(groupName);
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("groupName", groupInfo.getName());
        model.addAttribute("groupDescription", groupInfo.getDescription());
        model.addAttribute("pageMessage", "修改权限组");
        model.addAttribute("readOnly", true);
        model.addAttribute("includeUrl", "group/group_form.ftl");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/form";
    }

    @RequestMapping(value = "group_edit.vpage", method = RequestMethod.POST)
    public String group_edit_post(Model model) {

        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
        String groupDescription = getRequestParameter("groupDescription", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        if (groupDescription.equals("")) {
            getAlertMessageManager().addMessageError("权限组中文描述不能为空");
        }
        if (groupDescription.length() > 255) {
            getAlertMessageManager().addMessageError("权限组中文名称过长");
        }

        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "group/group_form.ftl");
            model.addAttribute("pageMessage", "修改权限组");
            model.addAttribute("groupName", groupName);
            model.addAttribute("groupDescription", groupDescription);
            model.addAttribute("showAdmin", managementService.superAdmin(adminName));
            return "management/form";
        }
        AdminGroup newGroup = new AdminGroup();
        newGroup.setDescription(groupDescription);
        if (adminGroupPersistence.update(groupName, newGroup)) {
            getAlertMessageManager().addMessageSuccess("修改权限组" + groupDescription + "成功");
            addAdminLog("editGroup", groupName, "成功", newGroup);
        } else {
            getAlertMessageManager().addMessageError("修改权限组" + groupDescription + "失败");
            addAdminLog("editGroup", groupName, "失败", newGroup);
        }
        return redirect("/management/group/list.vpage");
    }

    @RequestMapping(value = "group_member.vpage", method = RequestMethod.GET)
    public String group_member(Model model) {
        String groupName = getRequestParameter("name", "").replaceAll("\\s", "");
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String adminUserName = getRequestParameter("adminUserName", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        if (!groupListForWrite.contains(groupName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        if (!departmentName.equals("") && !departmentListForWrite.contains(departmentName)) {
            getAlertMessageManager().addMessageError("您缺少权限查看此部门");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("groupUserInfo", adminGroupUserPersistence.withSelectFromTable("USER_NAME", "WHERE GROUP_NAME=? AND DISABLED=0")
                .useParamsArgs(groupName)
                .queryColumnValues(String.class).toString());
        List<AdminDepartment> departmentList = adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:names)").useParams(MiscUtils.m("names", departmentListForWrite)).queryAll();
        List<AdminUser> userList;
        if (departmentName.equals("")) {
            userList = AlpsFutureBuilder.<String, List<AdminUser>>newBuilder()
                    .ids(CollectionUtils.toLinkedHashSet(departmentListForWrite))
                    .generator(id -> adminUserServiceClient.getAdminUserService().findAdminUsersByDepartmentName(id))
                    .buildList()
                    .regularize()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            userList = adminUserServiceClient.getAdminUserService()
                    .findAdminUsersByDepartmentName(departmentName)
                    .getUninterruptibly();
        }
        if (StringUtils.isNotBlank(adminUserName)) {
            userList = userList.stream().filter(u -> StringUtils.equals(u.getAdminUserName(), adminUserName))
                    .collect(Collectors.toList());
        }

        // 分页处理下
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 10);
        Page<AdminUser> productPage = PageableUtils.listToPage(userList, pageable);
        model.addAttribute("userPage", productPage);
        model.addAttribute("currentPage", productPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", productPage.getTotalPages());
        model.addAttribute("hasPrev", productPage.hasPrevious());
        model.addAttribute("hasNext", productPage.hasNext());

        model.addAttribute("groupInfo", adminGroupPersistence.loadFromDatabase(groupName));
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("appPathRoleByGroup", adminPathPersistence.getAppPathRoleByGroup(groupName));
        model.addAttribute("appNames", managementService.getAppNames());
        model.addAttribute("roleNames", managementService.getRoleNames());
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("groupName", groupName);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("pageMessage", "权限组成员管理");
        model.addAttribute("adminUserName", adminUserName);
        return "management/group/group_member";
    }

    @RequestMapping(value = "group_member_edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object group_member_edit() {
        String doType = getRequestParameter("do", "").replaceAll("\\s", "");
        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        if (groupListForWrite.contains(groupName)) {
            if (!groupName.equals("") && !userName.equals("")) {
                AdminGroupUser groupUserInfo = adminGroupUserPersistence.withSelectFromTable("WHERE GROUP_NAME=? AND USER_NAME=?").useParamsArgs(groupName, userName).queryObject();
                AdminGroupUser newGroupUserInfo = new AdminGroupUser();
                if (groupUserInfo != null) {
                    if (doType.equals("add")) {
                        newGroupUserInfo.setDisabled(false);
                        adminGroupUserPersistence.update(groupUserInfo.getId(), newGroupUserInfo);
                        addAdminLog("addUserToGroup", userName, groupName);
                    } else if (doType.equals("del")) {
                        newGroupUserInfo.setDisabled(true);
                        adminGroupUserPersistence.update(groupUserInfo.getId(), newGroupUserInfo);
                        addAdminLog("delUserToGroup", userName, groupName);
                    }
                } else {
                    if (doType.equals("add")) {
                        newGroupUserInfo.setGroupName(groupName);
                        newGroupUserInfo.setUserName(userName);
                        adminGroupUserPersistence.persist(newGroupUserInfo);
                        addAdminLog("addUserToGroup", userName, groupName);
                    }
                }
            }
        }
        // fixme : 返回值是啥？ The same doubt with Zhao Rex
        return null;
    }

    @RequestMapping(value = "group_role.vpage", method = RequestMethod.GET)
    public String group_role(Model model) {
        String groupName = getRequestParameter("name", "").replaceAll("\\s", "");
        String pathName = getRequestParameter("pathName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<AdminPath> pathList = managementService.getPathListByAdmin(adminName, pathName);

        // 分页处理下
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 15);
        Page<AdminPath> productPage = PageableUtils.listToPage(pathList, pageable);
        model.addAttribute("pathPage", productPage);
        model.addAttribute("currentPage", productPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", productPage.getTotalPages());
        model.addAttribute("hasPrev", productPage.hasPrevious());
        model.addAttribute("hasNext", productPage.hasNext());

        model.addAttribute("groupInfo", adminGroupPersistence.loadFromDatabase(groupName));
        model.addAttribute("pathRoleList", managementService.getPathRoles());
        model.addAttribute("roleNames", managementService.getRoleNames());
        model.addAttribute("appNames", managementService.getAppNames());
        model.addAttribute("pathRoleIds", adminPathRoleGroupPersistence.withSelectFromTable("PATH_ROLE_ID", "WHERE GROUP_NAME=? AND DISABLED=0")
                .useParamsArgs(groupName).queryColumnValues().toString());
        model.addAttribute("pageMessage", "权限组授权-功能操作");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("groupName", groupName);
        model.addAttribute("pathName", pathName);
        return "management/group/group_role";
    }
}
