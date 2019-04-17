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

package com.voxlearning.utopia.service.workflow.provider.module.management;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.common.Singleton;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.workflow.api.entity.WorkFlowStatus;
import com.voxlearning.utopia.service.workflow.impl.WorkFlowConfigParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Singleton
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@RequestMapping("/workflow")
final public class WorkflowConfigController {

    public static final WorkflowConfigController INSTANCE = new WorkflowConfigController();

    @RequestMapping("index.do")
    public String index(Model model) {
        Map<String, Map<String, WorkFlowStatus>> testConfig = WorkFlowConfigParser.getWorkFlowConfig(Mode.TEST);
        Map<String, Map<String, WorkFlowStatus>> prodConfig = WorkFlowConfigParser.getWorkFlowConfig(Mode.PRODUCTION);
        model.addAttribute("testKeys", testConfig.keySet());
        model.addAttribute("prodKeys", prodConfig.keySet());
        model.addAttribute("currentVersion", WorkFlowConfigParser.currentVersion());
        return "workflow/index";
    }


    @RequestMapping("query_config.do")
    public String checkWorkFlowConfig(Model model,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) String mode) {
        Map<String, Map<String, WorkFlowStatus>> configMap;
        if ("prod".equals(mode)) {
            configMap = WorkFlowConfigParser.getWorkFlowConfig(Mode.PRODUCTION);
        } else {
            configMap = WorkFlowConfigParser.getWorkFlowConfig(Mode.TEST);
        }
        if (StringUtils.isNotBlank(type)) {
            model.addAttribute("config", JsonUtils.toJsonPretty(configMap.get(type)));
        }
        model.addAttribute("mode", mode);
        model.addAttribute("type", type);
        return "workflow/config";
    }

}
