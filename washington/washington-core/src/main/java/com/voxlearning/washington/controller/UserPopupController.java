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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.misc.UserPopup;
import com.voxlearning.utopia.service.popup.client.LegacyPopupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/userpopup")
@Slf4j
public class UserPopupController extends AbstractController {

    @Inject private LegacyPopupServiceClient legacyPopupServiceClient;

    @RequestMapping(value = "getuserpopups.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getUserPopups() {
        User user = currentUser();
        if (user != null) {
            List<UserPopup> userPopupList = legacyPopupServiceClient.getLegacyPopupService()
                    .currentAvailablePopups(user.toSimpleUser(), 3, 5 * 60 * 1000);
            List<String> htmlList = new ArrayList<>();
            for (UserPopup userPopup : userPopupList) {
                htmlList.add(userPopup.getContent());
            }
            return MapMessage.successMessage().add("htmlList", htmlList);
        }
        return MapMessage.errorMessage();
    }
}
