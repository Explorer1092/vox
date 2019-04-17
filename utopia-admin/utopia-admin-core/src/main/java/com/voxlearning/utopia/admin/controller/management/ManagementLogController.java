/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.persist.entity.AdminDepartment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-5
 * Time: 下午5:19
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management/log")
public class ManagementLogController extends ManagementAbstractController {

    private List<String> logActionNames() {
        return Arrays.asList("delUserToGroup",
                "addUserToGroup",
                "addNewGroup",
                "editGroup",
                "addGroupToRole",
                "delGroupToRole",
                "addNewPath",
                "addRoleToPath",
                "delRoleToPath",
                "addNewDepartment",
                "addMasterToDepartment",
                "addDepartmentRightToMaster",
                "delDepartmentRightToMaster",
                "addMasterToGroup",
                "delMasterToGroup",
                "addNewApp",
                "editApp",
                "saveAdminUser",
                "editAdminUser");
    }

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        int pageNumber = Integer.parseInt(getRequestParameter("pageNumber", "1"));
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> departmentListForWrite = managementService.getDepartmentList(adminName, "write");
        String departmentName = getRequestParameter("departmentName", "").replaceAll("\\s", "");
        String logAction = getRequestParameter("logAction", "").replaceAll("\\s", "");
        String adminUser = getRequestParameter("adminUser", "").replaceAll("\\s", "");
        String targetUser = getRequestParameter("targetUser", "").replaceAll("\\s", "");
        List<String> logActionList = logActionNames();

        if (departmentName.length() == 0) {
            getAlertMessageManager().addMessageInfo("缺少权限访问此页面");
            return "management/index";
        }

        Boolean showAdmin = managementService.superAdmin(adminName);
        model.addAttribute("showAdmin", showAdmin);

        String where = " al.OPERATION IN (:a) AND au.DEPARTMENT_NAME IN (:e) ";
        Map<String, Object> param = new HashMap<>();

        param.put("a", StringUtils.isNotBlank(logAction) ? Arrays.asList(logAction) : logActionList);
        param.put("e", departmentListForWrite);

        if (StringUtils.isNotBlank(adminUser)) {
            where += " AND al.ADMIN_USER_NAME=:b ";
            param.put("b", adminUser);
        }

        if (StringUtils.isNotBlank(targetUser)) {
            where += " AND al.TARGET_STR=:c ";
            param.put("c", targetUser);
        }

        if (StringUtils.isNotBlank(departmentName)) {
            where += " AND au.DEPARTMENT_NAME=:d ";
            param.put("d", departmentName);
        }


        Pageable pageable = PageableUtils.startFromOne(pageNumber, 30);
        Page adminLogPage = adminLogPersistence.getUtopiaSql()
                .withSqlPage(pageable)
                .select(" al.ID, al.ADMIN_USER_NAME, al.TARGET_STR, al.OPERATION, al.CREATE_DATETIME, al.COMMENT, ad.DESCRIPTION")
                .from("ADMIN_LOG al JOIN ADMIN_USER au USING(ADMIN_USER_NAME) JOIN ADMIN_DEPARTMENT ad ON(ad.NAME=au.DEPARTMENT_NAME)")
                .where(where).orderBy("ID DESC").useParams(param).queryPage();

        List<AdminDepartment> departmentList = adminDepartmentPersistence.withSelectFromTable("WHERE NAME IN (?)").useParams(departmentListForWrite).queryAll();

        model.addAttribute("adminLogPage", adminLogPage);
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("departmentList", departmentList);
        model.addAttribute("departmentName", departmentName);
        model.addAttribute("logActionList", logActionList);
        model.addAttribute("logAction", logAction);
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("targetUser", targetUser);
        return "management/log/list";
    }

    @RequestMapping(value = "admin.vpage", method = RequestMethod.GET)
    public String admin(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        Boolean showAdmin = managementService.superAdmin(adminName);
        model.addAttribute("showAdmin", showAdmin);
        if (showAdmin == null) {
            getAlertMessageManager().addMessageInfo("开发中请稍后");
            return "management/index";
        }
        return "management/log/admin";
    }
}
