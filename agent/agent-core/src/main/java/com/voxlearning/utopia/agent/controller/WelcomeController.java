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

package com.voxlearning.utopia.agent.controller;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.task.TaskService;
import com.voxlearning.utopia.agent.service.workflow.AgentWorkflowService;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowTargetUserProcessData;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by Alex on 14-7-3.
 */
@Controller
@RequestMapping("/")
public class WelcomeController extends AbstractAgentController {

    @Inject AgentNotifyService agentNotifyService;
    @Inject TaskService taskService;
    @Inject
    private AgentWorkflowService agentWorkflowService;

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        model.addAttribute("msgs", agentNotifyService.getUnreadNotifiesCount(getCurrentUserId()));
        List<Map<String, Object>> taskList = taskService.getAssignments(getCurrentUserId());
        List<WorkFlowTargetUserProcessData> workFlowList = agentWorkflowService.getTodoList(getCurrentUserId());
        Integer tasks = 0;
        if(CollectionUtils.isNotEmpty(taskList)){
            tasks += taskList.size();
        }
        Integer workflowCount = 0;
        if(CollectionUtils.isNotEmpty(workFlowList)){
            workflowCount += workFlowList.size();
        }
        model.addAttribute("tasks", tasks);
        model.addAttribute("workflowCount", workflowCount);

        return "index";
    }

    @RequestMapping(value = "loadchartdata.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage kpiChart() {
        MapMessage mapMessage = new MapMessage();
        Map<String, List<Map<String, Object>>> userMap = new HashMap<>();
        mapMessage.setSuccess(true);
        mapMessage.set("value", userMap);
        return mapMessage;
    }

    @RequestMapping("mobile/welcoming.vpage")
    @ResponseBody
    public Map<String, Object> mobileLoadWelcoming(){

        Map<String, Object> message = new HashMap<>();
        message.put("result", "success");
        String productId = getRequestString("productId"); // 客户端产品ID
        if(StringUtils.isBlank(productId) || (!Objects.equals(productId, "901") && !Objects.equals(productId, "900"))){
            message.put("result", "error");
            message.put("message", "产品ID错误");
            return message;
        }

        PageBlockContent pageBlockContent;
        if(Objects.equals(productId, "901")){
            pageBlockContent = fetchConfigContent("AGENT_WELCOME_PAGE", "IOS");
        }else {
            pageBlockContent = fetchConfigContent("AGENT_WELCOME_PAGE", "ANDROID");
        }
        if(pageBlockContent != null){
            message.put("startTime", pageBlockContent.getStartDatetime().getTime());
            message.put("endTime", pageBlockContent.getEndDatetime().getTime());
            Map<String, Object> contentMap = JsonUtils.fromJson(pageBlockContent.getContent());
            if(MapUtils.isNotEmpty(contentMap)){
                message.put("bigImgUrl", String.valueOf(contentMap.get("bigImgUrl")));
                message.put("smallImgUrl", String.valueOf(contentMap.get("smallImgUrl")));
                message.put("showSeconds", SafeConverter.toInt(contentMap.get("showSeconds"), 3));
                message.put("intervalSeconds", SafeConverter.toInt(contentMap.get("intervalSeconds"), 8 * 60 * 60));
            }
        }
        return message;
    }

    private PageBlockContent fetchConfigContent(String pageName, String blockName){
        PageBlockContent pageBlockContent = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName(pageName)
                .stream()
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .filter(p -> Objects.equals(blockName, p.getBlockName()))
                .findFirst().orElse(null);

        return pageBlockContent;
    }
}
