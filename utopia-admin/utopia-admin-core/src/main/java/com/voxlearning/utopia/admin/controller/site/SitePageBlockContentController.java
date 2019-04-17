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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.persist.entity.AdminUser;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.crm.client.AdminUserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/site/pageblockcontent")
public class SitePageBlockContentController extends SiteAbstractController {

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject private AdminUserServiceClient adminUserServiceClient;

    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        List<PageBlockContent> pageBlockContentList = crmConfigService.$loadPageBlockContents()
                .stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .sorted((o1, o2) -> {
                    long c1 = SafeConverter.toLong(o1.getCreateDatetime());
                    long c2 = SafeConverter.toLong(o2.getCreateDatetime());
                    return Long.compare(c2, c1);
                })
                .collect(Collectors.toList());

        AdminUser adminUser = adminUserServiceClient.getAdminUserService()
                .loadAdminUser(getCurrentAdminUser().getAdminUserName())
                .getUninterruptibly();
        model.addAttribute("isSuperAdmin", adminUser.getSuperAdmin());
        model.addAttribute("pageBlockContentList", pageBlockContentList);
        return "site/pageblockcontent/list";
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    public String edit(Model model, @RequestParam(required = false) Long id) {

        PageBlockContent pbc;
        if (id == null) {
            pbc = new PageBlockContent();
            pbc.setStartDatetime(new Date(System.currentTimeMillis()));
            pbc.setEndDatetime(new Date(Timestamp.valueOf("2038-01-01 00:00:00").getTime()));
            pbc.setDisabled(false);
        } else {
            pbc = crmConfigService.$loadPageBlockContent(id);
        }

        model.addAttribute("pageBlockContent", pbc);
        return "site/pageblockcontent/edit";
    }

    @RequestMapping(value = "edit.vpage", method = RequestMethod.POST)
    public String editPost(Model model, HttpServletRequest request) {

        Date startDatetime = DateUtils.stringToDate(request.getParameter("startDatetime"), "yyyy/MM/dd hh:mm:ss");
        if (startDatetime == null) {
            getAlertMessageManager().addMessageError("开始时间不能为空");
        }
        Date endDatetime = DateUtils.stringToDate(request.getParameter("endDatetime"), "yyyy/MM/dd hh:mm:ss");
        if (endDatetime == null) {
            getAlertMessageManager().addMessageError("结束时间不能为空");
        }
        String pageName = request.getParameter("pageName");
        if (StringUtils.isBlank(pageName)) {
            getAlertMessageManager().addMessageError("页面字段不能为空");
        }
        String blockName = request.getParameter("blockName");
        if (StringUtils.isBlank(blockName)) {
            getAlertMessageManager().addMessageError("位置不能为空");
        }
        String memo = request.getParameter("memo");
        if (StringUtils.isBlank(memo)) {
            getAlertMessageManager().addMessageError("备注不能为空");
        }
        String content = request.getParameter("content");
        if (StringUtils.isBlank(content)) {
            getAlertMessageManager().addMessageError("content不能为空");
        }
        PageBlockContent input = new PageBlockContent();
        input.setStartDatetime(startDatetime);
        input.setEndDatetime(endDatetime);
        input.setPageName(pageName);
        input.setBlockName(blockName);
        input.setContent(content);
        input.setMemo(memo);
        if (getRequestLong("id") > 0) {
            input.setId(getRequestLong("id"));
        }
        input.setDisabled(SafeConverter.toBoolean(request.getParameter("disabled")));
        input.setDisplayOrder(ConversionUtils.toInt(request.getParameter("displayOrder")));
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("pageBlockContent", input);
            return "site/pageblockcontent/edit";
        }
        if (input.getId() == null) {
            input = crmConfigService.$upsertPageBlockContent(input);
            addAdminLog("savePageBlockContent", input.getId());
        } else {
            if (input.getBlockName().equals("student_mobile_app")) {
                if (JsonUtils.fromJson(input.getContent()) == null) {
                    getAlertMessageManager().addMessageError("保存数据不是JSON格式，请校验！");
                    return "site/pageblockcontent/edit";
                }
            }
            input.setCreateDatetime(null);
            PageBlockContent sourcePageBlockContent = crmConfigService.$loadPageBlockContent(input.getId());
            input = crmConfigService.$upsertPageBlockContent(input);
            addAdminLog("updatePageBlockContent", input.getId(), null, "before->" + JsonUtils.toJson(sourcePageBlockContent));
        }

        getAlertMessageManager().addMessageSuccess("保存完成");

        model.addAttribute("pageBlockContent", crmConfigService.$loadPageBlockContent(input.getId()));
        return "site/pageblockcontent/edit";
    }

    @RequestMapping(value = "simpleedit.vpage", method = RequestMethod.GET)
    public String simpleEdit(Model model) {
        model.addAttribute("isTest", RuntimeMode.le(Mode.TEST));
        return "site/pageblockcontent/simpleedit";
    }


    @RequestMapping(value = "getpagecontentbyid.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getPageContentById() {
        long id = getRequestLong("id", Long.MIN_VALUE);
        if (id == Long.MIN_VALUE) {
            return MapMessage.errorMessage("id不能为空");
        } else {
            PageBlockContent pbc = crmConfigService.$loadPageBlockContent(id);
            return MapMessage.successMessage().add("pbc", pbc);
        }
    }

    @RequestMapping(value = "updatecontentbyid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateContentById() {
        long id = getRequestLong("id", Long.MIN_VALUE);
        String content = getRequestString("content");
        if (StringUtils.isEmpty(content)) {
            return MapMessage.errorMessage("内容不能为空");
        }
        PageBlockContent pageBlockContent = crmConfigService.$loadPageBlockContent(id);
        if (pageBlockContent == null) {
            return MapMessage.errorMessage("id不存在");
        }
        // 这里需要对content做一下简单json检验
        pageBlockContent.setContent(content);
        crmConfigService.$upsertPageBlockContent(pageBlockContent);
        return MapMessage.successMessage("更新成功");
    }

    @RequestMapping(value = "delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delete(@RequestParam Long id) {
        if (crmConfigService.$disablePageBlockContent(id)) {
            return MapMessage.successMessage();
        } else {
            return MapMessage.errorMessage("删除失败");
        }
    }
}
