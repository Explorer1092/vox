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

package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.service.user.UserConfigService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collection;

/**
 *
 * Created by Shuai.Huan on 2014/7/18.
 */
@Controller
@RequestMapping("/sysconfig/password")
@Slf4j
public class PasswordController extends AbstractAgentController {

    @Inject
    private UserConfigService userConfigService;
    @Inject
    private AsyncLogService asyncLogService;
    @Inject
    private AgentCacheSystem agentCacheSystem;
    @Inject
    private BaseUserService baseUserService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isAdmin() || currentUser.getUserId() == 1712 || currentUser.getUserId() == 1699 || currentUser.getUserId() == 1300) {
            Collection<AgentUser> allUsers = baseUserService.getAllAgentUsers().values();
            model.addAttribute("users", allUsers);
        } else {
            // FIXME 
            // model.addAttribute("users", userConfigService.getChargedUsers(getCurrentUser()));
        }

        return "sysconfig/password/passwordindex";
    }

    @RequestMapping(value = "reset.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("3dfa0757785e4fec")
    MapMessage reset(Long userId, String newPassword) {
        asyncLogService.logResetPassword(getCurrentUser(), getRequest().getRequestURI(), "reset agent's password,userId:" + userId, "");
        return userConfigService.resetPassword(userId, "", newPassword, false);
    }

    @RequestMapping(value = "clear_device_id.vpage", method = RequestMethod.POST)
    @ResponseBody
    @OperationCode("3dfa0757785e4fec")
    MapMessage clearDeviceId(Long userId) {
        asyncLogService.logClearDeviceIdOperation(getCurrentUser(), getRequest().getRequestURI(), "clear agent's device,userId:" + userId, "");
        AgentUser agentUser = baseUserService.getById(userId);
        if(agentUser == null){
            return MapMessage.errorMessage("该用户不存在");
        }
        agentUser.setDeviceId("");
        baseUserService.updateAgentUser(agentUser);
        // 删除cache数据
        try {
            agentCacheSystem.removeUserSession(agentUser.getId());
            agentCacheSystem.getAlertMessageCache().evict(agentUser.getId());
        } catch (Exception e) {
        }
        return MapMessage.successMessage("清除成功");
    }
}
