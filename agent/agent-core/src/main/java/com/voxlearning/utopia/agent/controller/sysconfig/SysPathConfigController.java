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

package com.voxlearning.utopia.agent.controller.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPath;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.service.sysconfig.SysPathConfigService;
import com.voxlearning.utopia.agent.service.user.UserConfigService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;

/**
 * 系统功能权限管理用Contrller Class。
 * <p>
 * Created by Alex on 14-7-5.
 */
@Controller
@RequestMapping("/sysconfig/syspath")
@Slf4j
public class SysPathConfigController extends AbstractAgentController {

    @Inject SysPathConfigService sysPathConfigService;
    @Inject UserConfigService userConfigService;
    @Inject BaseUserService baseUserService;
    @Inject AsyncLogService asyncLogService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    String index(Model model) {
        model.addAttribute("allAgentRoleMap", AgentRoleType.getAllAgentRoles());
        model.addAttribute("sysPathList", sysPathConfigService.getAllSysPathList());
        return "sysconfig/syspath/syspathindex";
    }


    @RequestMapping(value = "addsyspath.vpage", method = RequestMethod.GET)
    String toAddSysPathPage(Model model) {
        long id = getRequestLong("id", 0);
        if (id != 0) {
            model.addAttribute("pathId", id);
            model.addAttribute("agentSysPath", sysPathConfigService.getAgentSysPathById(id));
        }
        model.addAttribute("allAgentRoleMap", AgentRoleType.getAllAgentRoles());
        return "sysconfig/syspath/addsyspath";
    }


    @RequestMapping(value = "addsyspath.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addSysPath(String appName, String pathName, String desc) {

        MapMessage message = new MapMessage();
        Long pathId = getRequestLong("pathId", 0);
        String[] roles = getRequest().getParameterValues("roles[]");
        List<Integer> roleList = StringUtils.toIntegerList(StringUtils.join(roles, ","));
        try {
            if (pathId == 0) {
                if (sysPathConfigService.sysPathExist(appName, pathName)) {
                    message.setSuccess(false);
                    message.setInfo("此权限路径已存在!");
                    return message;
                }
                pathId = sysPathConfigService.addSysPath(appName, pathName, desc, roleList);
            } else {
                AgentSysPath existPath = sysPathConfigService.getByName(appName, pathName);
                if (existPath != null && !existPath.getId().equals(pathId)) {
                    message.setSuccess(false);
                    message.setInfo("此权限路径已存在!");
                    return message;
                }
                sysPathConfigService.updateSysPath(pathId, appName, pathName, desc, roleList);
            }
            asyncLogService.logSysPathModified(getCurrentUser(), getRequest().getRequestURI(), "saveSysPath:" + pathId,
                    appName + "," + pathName + "," + desc + "," + roleList);
            message.setSuccess(true);
        } catch (Exception ex) {
            log.error("添加/编辑权限失败,appName:{},pathName:{},desc:{},roles:{},msg:{}",
                    appName, pathName, desc, roleList, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("操作失败!" + ex.getMessage());
            return message;
        }

        return message;
    }

    @RequestMapping(value = "delsyspath.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delSysPath(Long id) {

        MapMessage message = new MapMessage();
        try {
            sysPathConfigService.deleteSysPath(id);
            message.setSuccess(true);
        } catch (Exception ex) {
            log.error("删除权限失败,roleId:{},msg:{}", id, ex.getMessage(), ex);
            message.setSuccess(false);
            message.setInfo("操作失败!" + ex.getMessage());
            return message;
        }

        return message;
    }

}
