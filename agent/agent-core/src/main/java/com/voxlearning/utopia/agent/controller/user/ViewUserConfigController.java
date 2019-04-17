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

package com.voxlearning.utopia.agent.controller.user;

/**
 * Created by Alex on 14-11-3.
 */


import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.user.ViewUserConfigService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * 察看数据账户设置用Controller 类
 * Created by Shuai.Huan on 2014/7/10.
 */
@Controller
@RequestMapping("/user/viewuserconfig")
@Slf4j
public class ViewUserConfigController extends AbstractAgentController {

    @Inject ViewUserConfigService viewUserConfigService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        model.addAttribute("users", viewUserConfigService.getManagedViewUserList(getCurrentUserId()));
        return "user/viewuserconfig/index";
    }

    @RequestMapping(value = "addviewuser.vpage", method = RequestMethod.GET)
    String toAddViewUserPage(Model model) {
        long id = getRequestLong("id", 0);

        if (id != 0) {
            AgentUser agentUser = baseUserService.getById(id);
            model.addAttribute("userId", id);
            model.addAttribute("agentUser", agentUser);
        }

        return "user/viewuserconfig/addviewuser";
    }

    /**
     * 加载协作区域的JsonTree
     *
     * @return 协作区域JsonTree String
     */
    @RequestMapping(value = "loadregiontree.vpage", method = RequestMethod.GET)
    @ResponseBody
    String loadGroupTree() {
        try {
            Long userId = getRequestLong("userId");
            List<Map<String, Object>> groupList = viewUserConfigService.loadViewUserRegions(getCurrentUser(), userId);

            return JsonUtils.toJson(groupList);
        } catch (Exception ex) {
            log.error("加载region失败，msg:{}", ex.getMessage(), ex);
            return "加载region失败";
        }
    }

    @RequestMapping(value = "addviewuser.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addViewUser(String accountName, String realName, String userComment, String password,
                           String tel, String email, String imAccount, String address) {

        MapMessage message = new MapMessage();

        Long userId = getRequestLong("userId", 0);
        String[] regions = getRequest().getParameterValues("regions[]");
        List<Integer> regionCodeList = StringUtils.toIntegerList(StringUtils.join(regions, ","));
        try {
            if (userId == 0) {
                AgentUser agentUser = baseUserService.getByAccountName(accountName);
                if (agentUser != null) {
                    message.setSuccess(false);
                    String info = "此用户名已存在,请换个用户名再尝试!";
                    if (agentUser.getStatus() == 9) {
                        info = "此用户名已存在，并且目前处于被关闭状态，请换个用户名再尝试！";
                    }
                    message.setInfo(info);
                    return message;
                }

                userId = viewUserConfigService.addViewUser(accountName, realName, password, tel, email, imAccount, address,
                        userComment, regionCodeList, getCurrentUserId());

            } else {
                viewUserConfigService.updateViewUser(userId, realName, userComment,
                        tel, email, imAccount, address, regionCodeList);
            }

            message.setSuccess(true);
            message.add("userId", userId);
        } catch (Exception ex) {
            log.error("添加/编辑用户失败,userName:{},realName:{},userComment:{},password:{}, regions:{},msg:{}",
                    accountName, realName, userComment, password, regionCodeList, ex.getMessage(), ex
            );
            message.setSuccess(false);
            message.setInfo("操作失败!" + ex.getMessage());
            return message;
        }

        return message;
    }

    @RequestMapping(value = "delviewuser.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delViewUser(Long id) {

        MapMessage message = new MapMessage();
        try {
            viewUserConfigService.deleteViewUser(id);
            message.setSuccess(true);
        } catch (Exception ex) {
            log.error("删除用户失败,userId:{},msg:{}", id, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("操作失败!" + ex.getMessage());
            return message;
        }
        return message;
    }
}
