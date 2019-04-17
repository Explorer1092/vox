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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.user.api.entities.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

/**
 * Created by Sadi.Wan on 2014/12/26.
 */
@Deprecated
@Controller
@RequestMapping("/crm/wintermission")
public class CrmWinterMissionController extends CrmAbstractController {

    @RequestMapping(value = "showmissionhistory.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String showMissionHistory(Model model) {
        Long userId = getRequestLong("userId", -1L);
        User student = userLoaderClient.loadUser(userId, UserType.STUDENT);
        // FIXME: 不在支持这个查询了
        model.addAttribute("missionHistoryList", Collections.emptyList());
        model.addAttribute("userName", student == null ? "" : student.fetchRealname());
        model.addAttribute("userId", userId);
        return "crm/wintermission/showmissionhistory";
    }

    @RequestMapping(value = "importnohw.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    @Deprecated
    public MapMessage importNoHw(MultipartFile file) {
        return MapMessage.errorMessage("Unsupported");
    }
}
