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

package com.voxlearning.utopia.agent.controller.workspace;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.IssuedNotification;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 天权消息下发
 * Created by Shuai.Huan on 2014/8/13.
 */
@Controller
@RequestMapping("/workspace")
@Slf4j
public class UserManageController extends AbstractAgentController {

    @Inject private AgentNotifyService agentNotifyService;
    @Inject private BaseOrgService baseOrgService;


    @RequestMapping(value = "notifysend/index.vpage", method = RequestMethod.GET)
    public String toSendBatchNotifyPage(Model model) {
        model.addAttribute("applyRole", IssuedNotification.values());
        return "workspace/manage/sendnotifyindex";
    }

    //批量下发通知
    @RequestMapping(value = "notifysend/send.vpage", method = RequestMethod.POST)
    public String sendBatchNotify(@RequestParam(value = "title") String title,
                                  @RequestParam(value = "content") String content,
                                  @RequestParam(value = "groups") String groups,
                                  Model model) {
        if (StringUtils.isBlank(title) || title.length() > 50) {
            model.addAttribute("error", "标题长度过长");
            model.addAttribute("applyRole", IssuedNotification.values());
            return "workspace/manage/sendnotifyindex";
        }
        if (StringUtils.isBlank(content) || content.length() > 100) {
            model.addAttribute("error", "内容字数不能超过100");
            model.addAttribute("applyRole", IssuedNotification.values());
            return "workspace/manage/sendnotifyindex";
        }
        Set<Integer> roleId = new HashSet<>();
        Arrays.stream(IssuedNotification.values()).forEach(p -> {
            String checkRoleId = requestString("role_" + p.getId());
            if (StringUtils.isNotBlank(checkRoleId)) {
                roleId.add(SafeConverter.toInt(checkRoleId));
            }
        });
        if (CollectionUtils.isEmpty(roleId)) {
            model.addAttribute("error", "适用角色为必填项");
            model.addAttribute("applyRole", IssuedNotification.values());
            return "workspace/manage/sendnotifyindex";
        }
        try {
            //找到所有待发送的群组，包括用户选择的群组及其所有的子群组
            Set<Long> allGroupsIds = new HashSet<>();
            List<AgentGroup> allGroups = new ArrayList<>();
            String[] groupIds = groups.split(",");
            for (String groupId : groupIds) {
                baseOrgService.getAllSubGroupList(allGroups, SafeConverter.toLong(groupId));
                allGroupsIds.add(Long.parseLong(groupId));
            }
            allGroupsIds.addAll(allGroups.stream().map(AgentGroup::getId).collect(Collectors.toList()));
            //找出所有群组中的所有用户

            List<AgentGroupUser> receivers = new ArrayList<>();
            for (Long groupId : allGroupsIds) {
                List<AgentGroupUser> userIdSet = baseOrgService.getAllGroupUsersByGroupId(groupId);
                if (CollectionUtils.isNotEmpty(userIdSet)) {
                    receivers.addAll(userIdSet);
                }
            }
            Set<Long> receiverIds = receivers.stream().filter(p -> p.getUserRoleId() != null && roleId.contains(p.getUserRoleId())).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            //抄送给自己一份
            receiverIds.add(getCurrentUserId());
            final String namePrefix = "marketing-notify-";
            String file1 = upload("file1", namePrefix);
            String file2 = upload("file2", namePrefix);
            AgentNotifyType notifyType = AgentNotifyType.fetchByType(getRequestInt("type"));
            // 校验目前可以通过这里下发的通知类型
            if (notifyType == null || (notifyType != AgentNotifyType.BATCH_SEND_FROM_USER && notifyType != AgentNotifyType.IMPORTANT_NOTICE)) {
                model.addAttribute("error", "无效的通知类型");
                model.addAttribute("applyRole", IssuedNotification.values());
                return "workspace/manage/sendnotifyindex";
            }
            agentNotifyService.sendNotify(notifyType.getType(), title, content,
                    new ArrayList<>(receiverIds), file1, file2);

            asyncLogService.logSendNotify(getCurrentUser(), getRequest().getRequestURI(), "Send Notify",
                    "content：" + content);
            if (StringUtils.isNotBlank(file1) && StringUtils.isNotBlank(file2)) {
                asyncLogService.logSendNotify(getCurrentUser(), getRequest().getRequestURI(), "Send Notify",
                        "file1：" + file1 + " file2:" + file2);
            }
            asyncLogService.logSendNotify(getCurrentUser(), getRequest().getRequestURI(), "Send Notify",
                    "receiverIds：" + groups);

        } catch (Exception ex) {
            log.error("群发通知失败,groups:{},content:{},msg:{}", groups, content, ex.getMessage(), ex);
            model.addAttribute("error", StringUtils.formatMessage("群发通知失败,groups:{},content:{},msg:{}", groups, content, ex.getMessage()));
        }
        return "redirect:index.vpage";
    }

}
