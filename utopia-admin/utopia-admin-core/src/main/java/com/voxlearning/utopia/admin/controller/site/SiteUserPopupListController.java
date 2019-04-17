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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.time.FastDateFormat;
import com.voxlearning.utopia.api.constant.PopupCategory;
import com.voxlearning.utopia.api.constant.PopupType;
import com.voxlearning.utopia.api.constant.UserPopupRuleType;
import com.voxlearning.utopia.service.popup.client.GlobalUserPopupServiceClient;
import com.voxlearning.utopia.service.popup.client.UserPopupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.GlobalUserPopup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.PopupCategory.LOWER_RIGHT;
import static com.voxlearning.utopia.api.constant.PopupType.DEFAULT_AD;

/**
 * @author Longlong Yu
 * @since 下午9:04,13-9-13.
 */
@Controller
@RequestMapping("/site/userpopup")
public class SiteUserPopupListController extends SiteAbstractController {

    @Inject private GlobalUserPopupServiceClient globalUserPopupServiceClient;
    @Inject private UserPopupServiceClient userPopupServiceClient;

    @RequestMapping(value = "userpopuplist.vpage", method = RequestMethod.GET)
    public String userPopupList() {
        return "site/userpopup/userpopuplist";
    }

    @RequestMapping(value = "userpopuplist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage createUserPopupList() {

        String userIdSetStr = getRequestParameter("popupUserId", "");
        String popupContent = getRequestParameter("popupContent", "").trim();
        String nextDateStr = getRequestParameter("nextDateTime", "");


        if (StringUtils.isBlank(popupContent))
            return MapMessage.errorMessage("弹窗内容不能为空");

        Date nextDateTime = new Date();
        FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
        try {
            if (StringUtils.isNotBlank(nextDateStr))
                nextDateTime = formatter.parse(nextDateStr);
        } catch (Exception ignored) {
            nextDateTime = new Date();
        }

        Set<Long> userIdSet = new HashSet<>();
        try {

            String[] userIdStrList = userIdSetStr.split("[,，\\s]+");
            for (String it : userIdStrList) {
                if (StringUtils.isNotBlank(it)) {
                    userIdSet.add(Long.valueOf(it));
                }
            }

        } catch (Exception ignored) {
            return MapMessage.errorMessage("存在不符合规范的用户名");
        }

        if (userIdSet.size() == 0)
            return MapMessage.errorMessage("未输入用户ID");

        for (Long it : userIdSet) {
            userPopupServiceClient.createPopup(it)
                    .content(popupContent)
                    .next(nextDateTime)
                    .type(DEFAULT_AD)
                    .category(LOWER_RIGHT)
                    .create();
        }

        return MapMessage.successMessage("创建弹窗成功");
    }

    @RequestMapping(value = "batchpopuphomepage.vpage", method = RequestMethod.GET)
    public String batchPopupHomepage() {
        return "site/userpopup/batchpopuphomepage";
    }

    @RequestMapping(value = "batchpopupsend.vpage", method = RequestMethod.POST)
    public String batchSendPopUp(@RequestParam String content, Model model) {
        if (StringUtils.isEmpty(content)) {
            return String.valueOf(MapMessage.errorMessage("弹窗消息内容不能为空"));
        }
        String nextDateStr = getRequestParameter("nextDateTime", "");

        Date nextDateTime = new Date();
        FastDateFormat formatter = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
        try {
            if (StringUtils.isNotBlank(nextDateStr))
                nextDateTime = formatter.parse(nextDateStr);
        } catch (Exception ignored) {
            nextDateTime = new Date();
        }

        String[] messages = content.split("\\n");
        List<String> lstSuccess = new ArrayList<>();
        List<String> lstFailed = new ArrayList<>();

        for (String m : messages) {
            String[] info = m.split("\\t");
            if (info.length < 2) {
                lstFailed.add(m);
                continue;
            }

            String userId = StringUtils.deleteWhitespace(info[0]);
            String text = StringUtils.deleteWhitespace(m.substring(userId.length(), m.length()));

            if (text.length() == 0) {
                lstFailed.add(m);
                continue;
            }

            try {
                MapMessage message = userPopupServiceClient.createPopup(Long.valueOf(userId))
                        .content(text)
                        .next(nextDateTime)
                        .type(DEFAULT_AD)
                        .category(LOWER_RIGHT)
                        .create();
                if (!message.isSuccess()) {
                    lstFailed.add(m);
                } else {
                    lstSuccess.add(m);
                }
            } catch (Exception ex) {
                lstFailed.add(m);
            }
            // admin log
            addAdminLog("message-管理员${currentAdminUser.adminUserName}批量发送弹窗消息",
                    "", null, "ID:" + userId + ", content:" + text);
        }

        model.addAttribute("successlist", lstSuccess);
        model.addAttribute("failedlist", lstFailed);
        return "/site/userpopup/batchpopuphomepage";
    }


    @RequestMapping(value = "globaluserpopup.vpage", method = RequestMethod.GET)
    String globalUserPopup(Model model) {
        final DayRange current = DayRange.current();
        List<GlobalUserPopup> list = globalUserPopupServiceClient.getGlobalUserPopupService()
                .loadAllGlobalUserPopupsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> !e.isDisabledTrue())
                .filter(e -> e.getPopupCategory() == PopupCategory.LOWER_RIGHT)
                .filter(source -> {
                    long e = source.getEndDatetime() == null ? 0 : source.getEndDatetime().getTime();
                    long s = source.getStartDatetime() == null ? 0 : source.getStartDatetime().getTime();
                    return e >= current.getStartTime() && s <= current.getEndTime();
                })
                .collect(Collectors.toList());
        model.addAttribute("globalUserPopups", list);
        return "site/userpopup/globaluserpopup";
    }

    @RequestMapping(value = "addglobaluserpopup.vpage", method = RequestMethod.GET)
    String addGlobalUserPopup(Model model) {
        Long popupId = getRequestLong("popupId");
        if (popupId > 0) {
            Map<Long, GlobalUserPopup> map = globalUserPopupServiceClient.getGlobalUserPopupService()
                    .loadAllGlobalUserPopupsFromDB()
                    .getUninterruptibly()
                    .stream()
                    .collect(Collectors.toMap(GlobalUserPopup::getId, Function.identity()));
            GlobalUserPopup popupItem = map.get(popupId);
            if (popupItem != null && popupItem.isDisabledTrue()) {
                popupItem = null;
            }
            model.addAttribute("popupItem", popupItem);
        }

        List<UserPopupRuleType> allRuleMap = Arrays.asList(UserPopupRuleType.values());
        model.addAttribute("allRuleMap", allRuleMap);

        return "site/userpopup/addglobaluserpopup";
    }

    @RequestMapping(value = "saveglobalpopup.vpage", method = RequestMethod.POST)
    String saveGlobalUserPopup(Model model) {
        Long popupId = getRequestLong("popupId");
        String title = getRequestString("title");
        String content = getRequestString("content");
        String[] rules = getRequest().getParameterValues("popupRule");
        String startDatetime = getRequestString("startDatetime");
        String endDatetime = getRequestString("endDatetime");

        GlobalUserPopup popupItem = new GlobalUserPopup();
        popupItem.setTitle(title);
        popupItem.setContent(content);
        popupItem.setPopupRules(StringUtils.join(rules, ","));
        popupItem.setStartDatetime(DateUtils.stringToDate(startDatetime, "yyyy-MM-dd HH:mm"));
        popupItem.setEndDatetime(DateUtils.stringToDate(endDatetime, "yyyy-MM-dd HH:mm"));

        boolean parameterCheckError = false;
        if (StringUtils.isEmpty(title)) {
            getAlertMessageManager().addMessageError("请输入消息标题!");
            parameterCheckError = true;
        }
        if (StringUtils.isEmpty(content)) {
            getAlertMessageManager().addMessageError("请输入消息内容!");
            parameterCheckError = true;
        }
        if (rules == null || rules.length == 0) {
            getAlertMessageManager().addMessageError("请选择消息发送对象!");
            parameterCheckError = true;
        }
        if (StringUtils.isEmpty(startDatetime)) {
            getAlertMessageManager().addMessageError("请输入弹窗开始时间!");
            parameterCheckError = true;
        }
        if (StringUtils.isEmpty(endDatetime)) {
            getAlertMessageManager().addMessageError("请输入弹窗结束时间!");
            parameterCheckError = true;
        }
        if (parameterCheckError) {
            model.addAttribute("popupItem", popupItem);
            List<UserPopupRuleType> allRuleMap = Arrays.asList(UserPopupRuleType.values());
            model.addAttribute("allRuleMap", allRuleMap);
            return "site/userpopup/addglobaluserpopup";
        }

        popupItem.setPopupType(PopupType.DEFAULT_AD);
        popupItem.setPopupCategory(PopupCategory.LOWER_RIGHT);
        popupItem.setDisabled(false);
        if (popupId > 0) {
            popupItem.setId(popupId);
            try {
                globalUserPopupServiceClient.getGlobalUserPopupService()
                        .updateGlobalPopup(popupItem)
                        .awaitUninterruptibly();
            } catch (Exception ex) {
                logger.error("Failed to delete global user popup [popupId={}]", popupId, ex);
            }
        } else {
            try {
                globalUserPopupServiceClient.getGlobalUserPopupService()
                        .createGlobalPopup(popupItem)
                        .awaitUninterruptibly();
            } catch (Exception ex) {
                logger.error("Failed to create global user popup", ex);
            }
        }

        return "redirect:globaluserpopup.vpage";
    }

    @RequestMapping(value = "delglobaluserpopup.vpage", method = RequestMethod.GET)
    String delGlobalUserPopup() {
        long popupId = getRequestLong("popupId");
        if (popupId != 0) {
            try {
                globalUserPopupServiceClient.getGlobalUserPopupService()
                        .deleteGlobalPopup(popupId)
                        .awaitUninterruptibly();
            } catch (Exception ex) {
                logger.error("Failed to delete global user popup [popupId={}]", popupId, ex);
            }
        }
        return "redirect:globaluserpopup.vpage";
    }
}
