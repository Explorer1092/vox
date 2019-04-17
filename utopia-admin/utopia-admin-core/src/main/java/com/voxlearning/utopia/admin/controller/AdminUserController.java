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

package com.voxlearning.utopia.admin.controller;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.admin.service.management.ManagementService;
import com.voxlearning.utopia.admin.util.AdminUserPasswordObscurer;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

@Controller
@RequestMapping("/adminuser")
public class AdminUserController extends AbstractAdminController {

    @Inject protected ManagementService managementService;
    @Inject private AdminUserServiceClient adminUserServiceClient;

    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    public String edit(Model model, @RequestParam(required = false) String adminUserName) {

        AdminUser adminUser;
        if (adminUserName == null) {
            adminUser = new AdminUser();
        } else {
            adminUser = adminUserServiceClient.getAdminUserService()
                    .loadAdminUser(adminUserName)
                    .getUninterruptibly();
        }

        // 加入部门列表
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentList = managementService.getDepartmentList(adminName, "read");
        model.addAttribute("departmentList", departmentList);

        model.addAttribute("adminUser", adminUser);
        return "adminuser/edit";
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.POST)
    public String editPost(Model model) {

        // 准备输入数据
        String adminUserName = getRequestParameter("adminUserName", "");
        String password = getRequestParameter("password", "");
        String realName = getRequestParameter("realName", "");
        String comment = getRequestParameter("comment", "");
        String departmentName = getRequestParameter("department", "");

        // 构造需要更新的entity
        AdminUser adminUser = new AdminUser();
        adminUser.setRealName(realName);
        adminUser.setComment(comment);
        if (!StringUtils.isBlank(password)) {
            adminUser.setPasswordSalt(RandomUtils.randomString(6));
            adminUser.setPassword(AdminUserPasswordObscurer.obscurePassword(password, adminUser.getPasswordSalt()));
        }
        if (StringUtils.isNotBlank(departmentName)) {
            adminUser.setDepartmentName(departmentName);
        }

        // 检查数据有效性
        if (adminUserName.equals("")) getAlertMessageManager().addMessageError("用户名不能为空");

        if (adminUser.getRealName().equals("")) getAlertMessageManager().addMessageError("请填写真实姓名");

        // 客服外包团队必须以csos开头
        if ("csos".equals(departmentName) && !adminUserName.startsWith("csos"))
            getAlertMessageManager().addMessageError("客服外包用户名必须以csos开头");

        boolean userExists = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(adminUserName)
                .getUninterruptibly() != null;
        if (!userExists && password.equals("")) getAlertMessageManager().addMessageError("新用户请填写密码");

        // 如果没有问题，则进行插入或更新操作
        if (!getAlertMessageManager().hasMessageError()) {

            if (!userExists) {
                adminUser.setAdminUserName(adminUserName);
                adminUser.setCreateDatetime(new Timestamp(System.currentTimeMillis()));

                try {
                    adminUser = adminUserServiceClient.getAdminUserService()
                            .persistAdminUser(adminUser)
                            .getUninterruptibly();
                    addAdminLog("saveAdminUser", adminUser.getAdminUserName());
                } catch (Exception e) {
                    getAlertMessageManager().addMessageError(e.getMessage());
                }

            } else {
                adminUser.setAdminUserName(adminUserName);
                adminUser = adminUserServiceClient.getAdminUserService()
                        .modifyAdminUser(adminUser)
                        .getUninterruptibly();
                addAdminLog("updateAdminUser", adminUser.getAdminUserName());
            }


            if (!getAlertMessageManager().hasMessageError()) getAlertMessageManager().addMessageSuccess("保存完成");
        }


        model.addAttribute("userExists", userExists);

        if (getAlertMessageManager().hasMessageError() && !userExists) {
            // 如果新建用户遇到问题，则把构造的adminUser传回页面
            model.addAttribute("adminUser", adminUser);
        } else {
            // 把更新后的结果返回页面
            model.addAttribute("adminUser", adminUserServiceClient.getAdminUserService()
                    .loadAdminUser(adminUserName)
                    .getUninterruptibly());
        }

        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentList = managementService.getDepartmentList(adminName, "read");
        model.addAttribute("departmentList", departmentList);

        return "adminuser/edit";
    }

}
