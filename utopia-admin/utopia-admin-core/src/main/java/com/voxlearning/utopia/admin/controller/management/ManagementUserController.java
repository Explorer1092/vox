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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.admin.persist.entity.AdminDepartment;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.admin.util.AdminUserPasswordObscurer;
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
 * Time: 下午2:46
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management/user")
public class ManagementUserController extends ManagementAbstractController {

    @Inject private AdminUserServiceClient adminUserServiceClient;

    public Boolean hasUserEditPower(String userName, String adminName) {
        if (managementService.superAdmin(adminName)) {
            return true;
        } else {
// FIXME: 这里原来的代码位于AdminUserPersistence，是这么写的。。SQL的字段都不正确。。压根就没法执行
//            String sql = "SELECT COUNT(*) FROM ADMIN_DEPARTMENT_MASTER adm " +
//                    "JOIN ADMIN_USER au ON(adm.DEPARTMENT_NAME=au.DEPARTMENT_NAME) " +
//                    "WHERE au.USER_NAME=? AND adm.USER_NAME=?";
//            return utopiaSql.withSql(sql).useParamsArgs(userName, AdminName).queryValue(Long.class) > 0;
//            return adminUserPersistence.hasUserEditPower(userName, adminName);
            throw new UnsupportedOperationException("这里就没有正确工作过");
        }
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();

        List<String> departmentListForRead = managementService.getDepartmentList(adminName, "read");
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        List<String> departmentListForDelete = managementService.getDepartmentList(adminName, "delete");
        if (!managementService.superAdmin(adminName) && departmentListForRead.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("pageMessage", "用户");
        if (!departmentListForRead.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("a", departmentListForRead);
            model.addAttribute("departmentList", adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
        }
        List<AdminUser> userList = new ArrayList<>();
        if (departmentName.equals("") && !departmentListForRead.isEmpty()) {
            userList = AlpsFutureBuilder.<String, List<AdminUser>>newBuilder()
                    .ids(CollectionUtils.toLinkedHashSet(departmentListForRead))
                    .generator(id -> adminUserServiceClient.getAdminUserService().findAdminUsersByDepartmentName(id))
                    .buildList()
                    .regularize()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } else {
            if (departmentListForRead.contains(departmentName)) {
                userList = adminUserServiceClient.getAdminUserService()
                        .findAdminUsersByDepartmentName(departmentName)
                        .getUninterruptibly();
            } else {
                if (!managementService.superAdmin(adminName))
                    getAlertMessageManager().addMessageError("您缺少权限查看此部门");
                if (!departmentListForRead.isEmpty()) {
                    userList = AlpsFutureBuilder.<String, List<AdminUser>>newBuilder()
                            .ids(CollectionUtils.toLinkedHashSet(departmentListForRead))
                            .generator(id -> adminUserServiceClient.getAdminUserService().findAdminUsersByDepartmentName(id))
                            .buildList()
                            .regularize()
                            .stream()
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                }
            }
        }
        model.addAttribute("userList", userList);
        model.addAttribute("departmentNames", managementService.getDepartmentNames());
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("departmentForWrite", departmentListForWrite.toString());
        model.addAttribute("departmentForDelete", departmentListForDelete.toString());
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/user/list";
    }

    @RequestMapping(value = "getadminusers.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getAdminUsers() {
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForRead = managementService.getDepartmentList(adminName, "read");
        List<AdminUser> userList = AlpsFutureBuilder.<String, List<AdminUser>>newBuilder()
                .ids(CollectionUtils.toLinkedHashSet(departmentListForRead))
                .generator(id -> adminUserServiceClient.getAdminUserService().findAdminUsersByDepartmentName(id))
                .buildList()
                .regularize()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return MapMessage.successMessage().add("users", userList);
    }

    @RequestMapping(value = "new.vpage", method = RequestMethod.GET)
    public String new_user(Model model) {
        model.addAttribute("pageMessage", "新建用户");
        model.addAttribute("includeUrl", "user/user_form.ftl");
        model.addAttribute("departmentName", "");
        model.addAttribute("superAdmin", false);
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        if (!departmentListForWrite.isEmpty()) {
            Map<String, Object> params = new HashMap<>();
            params.put("a", departmentListForWrite);
            model.addAttribute("departmentList", adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
        }
        return "management/form";
    }


    @RequestMapping(value = "new.vpage", method = RequestMethod.POST)
    public String new_user_post(Model model) {
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String realName = StringUtils.trim(getRequestParameter("realName", ""));
        String password = getRequestParameter("password", "").replaceAll("\\s", "");
        boolean beSuperAdmin = Boolean.parseBoolean(getRequestParameter("superAdmin", "0"));

        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");

        Map<String, Object> params = new HashMap<>();
        params.put("a", departmentListForWrite);
        List<AdminDepartment> departmentList = adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll();
        model.addAttribute("pageMessage", "新建用户");
        model.addAttribute("includeUrl", "user/user_form.ftl");
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("userName", userName);
        model.addAttribute("password", password);
        model.addAttribute("superAdmin", beSuperAdmin);
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("departmentList", departmentList);

        // 检查数据有效性
        if (userName.equals("")) {
            getAlertMessageManager().addMessageError("用户名不能为空");
            return "management/form";
        }
        boolean userExists = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(userName)
                .getUninterruptibly() != null;
        if (userExists) {
            getAlertMessageManager().addMessageError("用户名已存在");
            return "management/form";
        }
        if (realName.equals("")) {
            getAlertMessageManager().addMessageError("请填写真实姓名");
            return "management/form";
        }
        if (departmentName.equals("")) {
            getAlertMessageManager().addMessageError("用户部门不能为空");
            return "management/form";
        }

        if (!departmentListForWrite.contains(departmentName)) {
            getAlertMessageManager().addMessageError("请选择正确的部门");
            return "management/form";
        }


        AdminUser adminUser = new AdminUser();
        adminUser.setRealName(realName);
        if (!StringUtils.isBlank(password)) {
            adminUser.setPasswordSalt(RandomUtils.randomString(6));
            adminUser.setPassword(AdminUserPasswordObscurer.obscurePassword(password, adminUser.getPasswordSalt()));
        }


        // 如果没有问题，则进行插入或更新操作
        if (!getAlertMessageManager().hasMessageError()) {

            if (!userExists) {
                adminUser.setAdminUserName(userName);
                adminUser.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
                adminUser.setDepartmentName(departmentName);
                adminUser.setSuperAdmin(beSuperAdmin);
                adminUser.setComment("");

                try {
                    adminUser = adminUserServiceClient.getAdminUserService()
                            .persistAdminUser(adminUser)
                            .getUninterruptibly();
                    addAdminLog("saveAdminUser", adminUser.getAdminUserName());
                } catch (Exception e) {
                    getAlertMessageManager().addMessageError(e.getMessage());
                }
            }

            if (!getAlertMessageManager().hasMessageError())
                getAlertMessageManager().addMessageSuccess("保存完成");
        }
        return redirect("/management/user/list.vpage");
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    public String edit(Model model) {
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        model.addAttribute("pageMessage", "修改用户");
        model.addAttribute("includeUrl", "user/user_form.ftl");
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        Map<String, Object> params = new HashMap<>();
        params.put("a", departmentListForWrite);
        model.addAttribute("departmentList", adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
        AdminUser userInfo = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(userName)
                .getUninterruptibly();
        model.addAttribute("userName", userInfo.getAdminUserName());
        model.addAttribute("realName", userInfo.getRealName());
        model.addAttribute("departmentName", userInfo.getDepartmentName());
        model.addAttribute("superAdmin", userInfo.getSuperAdmin());
        model.addAttribute("agentId", userInfo.getAgentId());
        model.addAttribute("isEdit", true);
        return "management/form";
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.POST)
    public String edit_user_post(Model model) {
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String userName = getRequestParameter("userName", "").replaceAll("\\s", "");
        String realName = StringUtils.trim(getRequestParameter("realName", ""));
        String password = getRequestParameter("password", "").replaceAll("\\s", "");
        String agentId = StringUtils.trim(getRequestParameter("agentId", ""));
        boolean beSuperAdmin = Boolean.parseBoolean(getRequestParameter("superAdmin", "0"));

        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        Map<String, Object> params = new HashMap<>();
        params.put("a", departmentListForWrite);
        List<AdminDepartment> departmentList = adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll();
        model.addAttribute("pageMessage", "修改用户");
        model.addAttribute("includeUrl", "user/user_form.ftl");
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("userName", userName);
        model.addAttribute("password", password);
        model.addAttribute("superAdmin", beSuperAdmin);
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("agentId", agentId);
        // 检查数据有效性
        if (userName.equals("")) {
            getAlertMessageManager().addMessageError("用户名不能为空");
            return "management/form";
        }
        if (!hasUserEditPower(userName, adminName)) {
            getAlertMessageManager().addMessageError("缺少权限修改此用户");
            return "management/form";
        }
        if (realName.equals("")) {
            getAlertMessageManager().addMessageError("请填写真实姓名");
            return "management/form";
        }
        if (departmentName.equals("")) {
            getAlertMessageManager().addMessageError("用户部门不能为空");
            return "management/form";
        }

        if (!departmentListForWrite.contains(departmentName)) {
            getAlertMessageManager().addMessageError("请选择正确的部门");
            return "management/form";
        }

        AdminUser oldAdminUser = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(userName)
                .getUninterruptibly();
        AdminUser adminUser = new AdminUser();
        if (!oldAdminUser.getRealName().equals(realName)) {
            adminUser.setRealName(realName);
        }
        if (!StringUtils.isBlank(password)) {
            adminUser.setPasswordSalt(RandomUtils.randomString(6));
            adminUser.setPassword(AdminUserPasswordObscurer.obscurePassword(password, adminUser.getPasswordSalt()));
        }
        // FIX 只修改AgentId而不能保存的问题 By Wyc 2016-06-15
//        if(StringUtils.isNotBlank(agentId)){
        adminUser.setAgentId(agentId);
//        }


        // 如果没有问题，则进行插入或更新操作
        if (!getAlertMessageManager().hasMessageError()) {
            if (!oldAdminUser.getDepartmentName().equals(departmentName)) {
                adminUser.setDepartmentName(departmentName);
            }
            if (!oldAdminUser.getSuperAdmin().equals(beSuperAdmin)) {
                adminUser.setSuperAdmin(beSuperAdmin);
            }
            try {
                adminUser.setAdminUserName(userName);
                adminUser = adminUserServiceClient.getAdminUserService()
                        .modifyAdminUser(adminUser)
                        .getUninterruptibly();
                addAdminLog("editAdminUser", userName);
            } catch (Exception e) {
                getAlertMessageManager().addMessageError(e.getMessage());
            }

            if (!getAlertMessageManager().hasMessageError())
                getAlertMessageManager().addMessageSuccess("修改完成");
        }
        return redirect("/management/user/list.vpage");
    }

    @RequestMapping(value = "user_group.vpage", method = RequestMethod.GET)
    public String user_group(Model model) {
        String userName = getRequestParameter("name", "").replaceAll("\\s", "");
        AdminUser userInfo = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(userName)
                .getUninterruptibly();
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> departmentListForRead = managementService.getDepartmentList(adminName, "read");
        if (!departmentListForRead.contains(userInfo.getDepartmentName())) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        model.addAttribute("userInfo", userInfo);
        model.addAttribute("departmentNames", managementService.getDepartmentNames());
        Map<String, Object> params = new HashMap<>();
        params.put("a", groupListForWrite);
        model.addAttribute("groupList", adminGroupPersistence.withSelectFromTable("WHERE NAME IN (:a)").useParams(params).queryAll());
        model.addAttribute("userGroupInfo", adminGroupUserPersistence.withSelectFromTable("GROUP_NAME", "WHERE USER_NAME=? AND DISABLED=0").useParamsArgs(userName).queryColumnValues().toString());
        model.addAttribute("pageMessage", "用户-权限组");
        return "management/user/user_group";
    }
}
