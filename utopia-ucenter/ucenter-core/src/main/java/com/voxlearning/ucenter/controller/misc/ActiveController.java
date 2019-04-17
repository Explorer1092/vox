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

package com.voxlearning.ucenter.controller.misc;

import com.voxlearning.alps.config.manager.ConfigManager;
import com.voxlearning.alps.lang.cipher.DesUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.exception.config.ConfigurationException;
import com.voxlearning.ucenter.support.controller.AbstractWebController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * @author changyuan.liu
 * @since 2015.12.16
 */
@Controller
@Slf4j
@RequestMapping("/active")
public class ActiveController extends AbstractWebController {

    @RequestMapping(value = "email/active_email.vpage", method = RequestMethod.GET)
    public String index(String code, Model model) {
        String legacy_des_key = ConfigManager.instance().getCommonConfig().getConfigs().get("legacy_des_key");
        if (legacy_des_key == null) {
            throw new ConfigurationException("No 'legacy_des_key' configured");
        }

        Long userId = null;
        String targetEmail = null;
        try {
            String decryptCode = DesUtils.decryptHexString(legacy_des_key, code);
            Map<String, Object> validationInfo = JsonUtils.fromJson(decryptCode);
            userId = ConversionUtils.toLong(validationInfo.get("userId"));
            targetEmail = ConversionUtils.toString(validationInfo.get("email"));
            long timestamp = ConversionUtils.toLong(validationInfo.get("timestamp"));
            if (System.currentTimeMillis() > timestamp + 86400000L || userLoaderClient.loadEmailAuthentication(targetEmail) != null) {
                model.addAttribute("state", false);
            } else {
                model.addAttribute("state", userServiceClient.activateUserEmail(userId, targetEmail).isSuccess());
            }
        } catch (Exception ex) {
            log.error("Error occurs when validating email {}/{}, the error message is {}", userId, targetEmail, ex.getMessage());
            model.addAttribute("state", false);
        }
        return "ucenter/activeemail";
    }
}
