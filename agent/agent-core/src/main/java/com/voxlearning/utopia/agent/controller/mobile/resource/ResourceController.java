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

package com.voxlearning.utopia.agent.controller.mobile.resource;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.resource.SchoolResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * 资源controller
 *
 */
@Controller
@RequestMapping("/mobile/resource")
public class ResourceController extends AbstractAgentController {
    @Inject
    private SchoolResourceService schoolResourceService;

    /**
     * 获取当前登录人（市经理及以上）所在部门的专员信息
     * @return
     */
    @RequestMapping(value = "business_developer_list.vpage")
    @ResponseBody
    public MapMessage businessDeveloperList(){
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()){
            return MapMessage.errorMessage("您无权限查询");
        }
        return MapMessage.successMessage().add("dataList",schoolResourceService.businessDeveloperList(currentUser));
    }

}
